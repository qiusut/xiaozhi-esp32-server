import asyncio
import json
import threading

from config.logger import setup_logging
from config.settings import redisClient
from core.utils.util import invoking_http_api
from plugins_func.register import register_function, ToolType, ActionResponse, Action

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

    tb_token = redisClient.get(f"tb:token")
    device_id = conn.headers.get("device-id", "").replace(':', '-')
    user_id = redisClient.get(f"device:{device_id}:user_id")

    user_key = f"tb:user:{user_id}:"
    fun_call = redisClient.hgetall(user_key+"fun_call")

    names_set = {json.loads(value)["name"] for key, value in fun_call.items()}

    if function_name == tb_fun:
        description = "能为您控制的tb智能设备为："
        if names_set:

            description += ", ".join(names_set) + "。"
        else:
            description = "您的账号下没有能控制的智能设备。"

        action_response.action = Action.RESPONSE

    else:
        sre_parse = function_name.split("_")
        device_views = json.loads(fun_call.get(sre_parse[2]))
        type = device_views["type"]
        tb_device_id = device_views["id"]
        if tb_names and len(names_set)>1:
            # 初始化匹配结果列表
            matched_devices = []

            for tb_name in tb_names:
                # 精确匹配
                if tb_name in names_set:
                    matched_devices.append(tb_name)
                else:
                    # 左模糊匹配
                    left_match = [e for e in names_set if (e.startswith(tb_name) and not e.endswith(tb_name))]
                    # 右模糊匹配
                    right_match = [e for e in names_set if (not e.startswith(tb_name) and e.endswith(tb_name))]
                    # 合并匹配结果
                    matched_devices.extend(left_match + right_match)

            # 去重
            if matched_devices:
                device_views = matched_devices

        fun_key = f"tb:device_fun:{type}:{sre_parse[1]}"
        method = redisClient.hget(fun_key,"method")
        if len(device_views) == 1:
            if tb_token:
                invoking_api = {
                    "url": tb_url + tb_rpc_url.format(deviceId=tb_device_id),
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
                    print(f"thingsBoard api调用失败: {response.status_code}")
                    if response.status_code == 504:
                        description = f"设置失败，检查一下设备是否在线"
                    elif response.status_code == 413:
                        description = f"设置失败，设备请求过多，拒绝你的请求"
                    elif response.status_code == 401:
                        description = f"设置失败，你没有控制该设备的权限"
                    elif response.status_code == 400:
                        description = f"设置失败，你打开的方式有问题，请联想管理员"
                    else:
                        logger.bind(tag=TAG).error(f"thingsBoard api调用失败: {response}")
                        description = f"设置失败，不知道怎么回事，返回的错误码为{response.status_code}"

                else:
                    description = "设置成功"
            else:
                description = "请先登录tb账号"

        else:
            names = ""
            for device_view in device_views:
                names += device_view + ","
            description = f"为您匹配到{len(device_views)}台设备,分别为{names}您要控制的是哪一台？"
            action_response.action = Action.RESPONSE

    action_response.response = description
    action_response.result = description

    return action_response
