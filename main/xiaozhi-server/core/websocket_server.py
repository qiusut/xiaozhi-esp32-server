import asyncio
import json

import websockets
from torch.cuda import device

from config.logger import setup_logging
from core.connection import ConnectionHandler
from core.handle.iotHandle import set_iot_status
from core.utils.util import initialize_modules, check_vad_update, check_asr_update
from config.config_loader import get_config_from_api
from aiohttp import web

TAG = __name__


class WebSocketServer:
    def __init__(self, config: dict):
        self.config = config
        self.logger = setup_logging()
        self.config_lock = asyncio.Lock()
        modules = initialize_modules(
            self.logger,
            self.config,
            "VAD" in self.config["selected_module"],
            "ASR" in self.config["selected_module"],
            "LLM" in self.config["selected_module"],
            "TTS" in self.config["selected_module"],
            "Memory" in self.config["selected_module"],
            "Intent" in self.config["selected_module"],
        )
        self._vad = modules["vad"] if "vad" in modules else None
        self._asr = modules["asr"] if "asr" in modules else None
        self._tts = modules["tts"] if "tts" in modules else None
        self._llm = modules["llm"] if "llm" in modules else None
        self._intent = modules["intent"] if "intent" in modules else None
        self._memory = modules["memory"] if "memory" in modules else None
        self.active_connections = set()

    async def start(self):
        server_config = self.config["server"]
        host = server_config.get("ip", "0.0.0.0")
        port = int(server_config.get("port", 8000))

        #async with websockets.serve(self._handle_connection, host, port, process_request=self._http_response):
        #    await asyncio.Future()

        # 创建两个服务任务
        ws_task = websockets.serve(
            self._handle_connection,
            host,
            port,
            process_request=self._http_response
        )

        http_task = self.httpApi()

        # 并行运行两个服务
        await asyncio.gather(ws_task, http_task)

    async def _handle_connection(self, websocket):
        """处理新连接，每次创建独立的ConnectionHandler"""
        # 创建ConnectionHandler时传入当前server实例
        handler = ConnectionHandler(
            self.config,
            self._vad,
            self._asr,
            self._llm,
            self._tts,
            self._memory,
            self._intent,
            self,  # 传入server实例
        )
        self.active_connections.add(handler)
        try:
            await handler.handle_connection(websocket)
        finally:
            self.active_connections.discard(handler)

    async def _http_response(self, websocket, request_headers):
        # 检查是否为 WebSocket 升级请求
        if request_headers.headers.get("connection", "").lower() == "upgrade":
            # 如果是 WebSocket 请求，返回 None 允许握手继续
            return None
        else:
            # 如果是普通 HTTP 请求，返回 "server is running"
            return websocket.respond(200, "Server is running\n")

    async def update_config(self) -> bool:
        """更新服务器配置并重新初始化组件

        Returns:
            bool: 更新是否成功
        """
        try:
            async with self.config_lock:
                # 重新获取配置
                new_config = get_config_from_api(self.config)
                if new_config is None:
                    self.logger.bind(tag=TAG).error("获取新配置失败")
                    return False

                # 检查 VAD 和 ASR 类型是否需要更新
                update_vad = check_vad_update(self.config, new_config)
                update_asr = check_asr_update(self.config, new_config)

                # 更新配置
                self.config = new_config
                # 重新初始化组件
                modules = initialize_modules(
                    self.logger,
                    new_config,
                    update_vad,
                    update_asr,
                    "LLM" in new_config["selected_module"],
                    "TTS" in new_config["selected_module"],
                    "Memory" in new_config["selected_module"],
                    "Intent" in new_config["selected_module"],
                )

                # 更新组件实例
                if "vad" in modules:
                    self._vad = modules["vad"]
                if "asr" in modules:
                    self._asr = modules["asr"]
                if "tts" in modules:
                    self._tts = modules["tts"]
                if "llm" in modules:
                    self._llm = modules["llm"]
                if "intent" in modules:
                    self._intent = modules["intent"]
                if "memory" in modules:
                    self._memory = modules["memory"]

                return True
        except Exception as e:
            self.logger.bind(tag=TAG).error(f"更新服务器配置失败: {str(e)}")
            return False

    async def httpApi(self):
        server_config = self.config["server"]
        host = server_config.get("ip", "0.0.0.0")
        port = int(server_config.get("http_port"))
        http_ws_url = server_config.get("http_ws_url", "/xiaozhi/websocket")

        if port:
            app = web.Application()
            # 添加路由
            app.add_routes(
                [
                    web.get(http_ws_url, self.get_websocket),
                    web.post(http_ws_url, self.send_websocket),
                ]
            )

            # 运行服务
            runner = web.AppRunner(app)
            await runner.setup()
            site = web.TCPSite(runner, host, port)
            await site.start()

            await asyncio. Future()

    async def get_websocket(self, request):
        status = 200

        devices = dict()

        try:
            device_mac = request.headers.get("device_mac", "").strip()

            for handler in self.active_connections:
                if not device_mac or handler.device_id == device_mac:
                    devices[handler.device_id] = {key: value.to_dict() for key, value in handler.iot_descriptors.items()}

            message = json.dumps(devices)
        except asyncio.CancelledError:
            self.logger.bind(tag=TAG).warning("请求被取消")
            status = 503
            message = "Request was cancelled"
        except Exception as e:
            self.logger.bind(tag=TAG).error(f"处理WebSocket推送异常: {e}")
            status = 500
            message = f"服务器异常: {e}"

        # 构造响应并设置CORS头部
        response = web.Response(text=message, content_type="text/plain", status=status)
        return response

    async def send_websocket(self, request):
        status = 200
        message = "OK"
        found = False

        try:
            # 获取请求参数
            device_mac = request.headers.get("device_mac", "").strip()
            body = await request.text()

            # 输入校验
            if not device_mac or not body:
                status = 400
                message = "Missing device_mac or body"
            else:
                # 遍历连接集合查找目标设备
                for handler in self.active_connections:
                    if handler.device_id == device_mac:
                        try:
                            # 发送消息并记录日志
                            await handler.websocket.send(body)
                            body_dict = json.loads(body)
                            for item in body_dict.get("commands", []):
                                name = item["name"]
                                parameters = item["parameters"]
                                if name and isinstance(parameters, dict) and parameters:
                                    for p_key, p_value in parameters.items():
                                        await set_iot_status(handler, name, p_key, p_value)

                            self.logger.bind(tag=TAG).info(f"http推送websocket消息: {body}")
                            message = "发送成功"
                            found = True
                        except websockets.exceptions.ConnectionClosed as e:
                            self.logger.bind(tag=TAG).error(f"WebSocket连接已关闭: {e}")
                            status = 503
                            message = f"WebSocket连接已断开: {e}"
                        except Exception as e:
                            self.logger.bind(tag=TAG).error(f"发送消息异常: {e}")
                            status = 500
                            message = f"内部错误: {e}"
                        break

                if not found:
                    status = 500
                    message = "Device not connected"

        except asyncio.CancelledError:
            self.logger.bind(tag=TAG).warning("请求被取消")
            status = 503
            message = "Request was cancelled"
        except Exception as e:
            self.logger.bind(tag=TAG).error(f"处理WebSocket推送异常: {e}")
            status = 500
            message = f"服务器异常: {e}"

        # 构造响应并设置CORS头部
        response = web.Response(text=message, content_type="text/plain", status=status)
        return response
