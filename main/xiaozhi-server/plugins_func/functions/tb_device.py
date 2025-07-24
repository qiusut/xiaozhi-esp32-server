import asyncio
import json
import threading

from config.logger import setup_logging
from config.settings import redisClient
from plugins_func.register import register_function, ToolType, ActionResponse, Action
from core.utils.util import invoking_http_api
from plugins_func.functions.tb_init import init_tb_token

TAG = __name__
logger = setup_logging()
tb_fun = "tb_device"

tb_rpc_url = "/api/rpc/oneway/{deviceId}"

tb_device_function_desc = {
    "type": "function",
    "function": {
        "name": tb_fun,
        "description": "获取可远程控制的设备列表，不涉及设备操作,只用于当用户要明确查询能够远程控制哪些设备时才触发。",
        "parameters": {
            "type": "object",
            "properties": {
            },
            "required": [],
            "description": "此函数不接受参数，仅返回用户可控制的设备列表。"
        }
    }
}


@register_function(tb_fun, tb_device_function_desc, ToolType.SYSTEM_CTL)
def tb_device(conn,function_name: str,tb_name=None,tb_args: dict=None):

    # 创建新的事件循环
    result = None
    new_loop = asyncio.new_event_loop()
    # 在新线程中运行事件循环（仅运行一次）
    loop_thread = threading.Thread(target=new_loop.run_forever, daemon=True)
    loop_thread.start()
    try:
        future = asyncio.run_coroutine_threadsafe(
            handle_tb_device(conn,function_name,tb_name,tb_args),
            new_loop
        )
        result = future.result()
    except Exception as e:
        logger.bind(tag=TAG).error(f"处理设置属性意图错误: {e}")

    finally:
        new_loop.call_soon_threadsafe(new_loop.stop)  # 停止事件循环
        loop_thread.join()  # 等待线程结束
        new_loop.close()

    return result

async def handle_tb_device(conn,function_name,tb_names,tb_args):
    logger.bind(tag=TAG).info(f"成功进入了tb的handle_tb_device方法: {function_name}")
    tb_url = redisClient.get('tb:url')

    action_response = ActionResponse(action=Action.REQLLM, result="执行成功", response=None)
    plugin_config = conn.config["plugins"]["tb_device"]

    if not plugin_config:
        action_response.action = Action.RESPONSE
        action_response.response = "未绑定tb账号，无法使用该功能"
        return action_response

    tb_username = plugin_config.get("tb_username")
    tb_password = plugin_config.get("tb_password")

    tb_token = init_tb_token(tb_username,tb_password)
    control_device_dict = redisClient.hgetall(f"tb:account:{tb_username}:control_device")

    if function_name == tb_fun:
        description = "能为您控制的tb智能设备为："
        if control_device_dict:

            device_set = {tb_view["name"] for tb_value in control_device_dict.values() for tb_view in json.loads(tb_value)}
            description += ", ".join(device_set) + "。"
        else:
            description = "您的账号下没有能控制的智能设备。"

        action_response.action = Action.RESPONSE

    else:
        sre_parse = function_name.split("_")
        device_views = json.loads(control_device_dict.get(sre_parse[1]))
        #tb_names = param_dict.get("tb_name", None)
        if tb_names and len(device_views)>1:
            # 初始化匹配结果列表
            matched_devices = []

            for tb_name in tb_names:
                # 左模糊匹配
                left_match = [e for e in device_views if (e["name"].startswith(tb_name) and not e["name"].endswith(tb_name))]
                # 右模糊匹配
                right_match = [e for e in device_views if (not e["name"].startswith(tb_name) and e["name"].endswith(tb_name))]
                # 精确匹配
                exact_match = [e for e in device_views if e["name"] == tb_name]

                # 合并匹配结果
                matched_devices.extend(left_match + right_match + exact_match)

            # 去重
            if matched_devices:
                device_views = matched_devices

        fun_key = f"tb:device_fun:{function_name.removeprefix('tb_').replace('_',':')}"
        method = redisClient.hget(fun_key,"method")
        if len(device_views) == 1:
            tb_deviceId = device_views[0]["id"]["id"]
            invoking_api = {
                "url": tb_url + tb_rpc_url.format(deviceId=tb_deviceId),
                "method": "POST",
                "headers": {
                    "Authorization": "Bearer "+tb_token
                },
                "body": {
                    "method": method,
                    "params": tb_args,
                    "persistent": False,
                    "timeout": 5000
                }
            }
            response = invoking_http_api(invoking_api)
            if response.status_code != 200:
                logger.bind(tag=TAG).error(f"thingsBoard api调用失败: {response}")
                description = f"设置失败，错误码: {response.status_code}"
            else:
                description = "设置成功"

        else:
            names = ""
            for device_view in device_views:
                names += device_view["name"] + ","
            description = f"为您匹配到{len(device_views)}台设备,分别为{names}您要控制的是哪一台？"
            action_response.action = Action.RESPONSE

    action_response.response = description

    return action_response
