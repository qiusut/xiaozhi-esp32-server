import asyncio
import json

from config.logger import setup_logging
from config.settings import redisClient
from plugins_func.register import register_function, ToolType, ActionResponse, Action

TAG = __name__
logger = setup_logging()

# 设备控制
recipe_device_function_desc = {
    "type": "function",
    "function": {
        "name": "recipe_device",
        "description": (
            "用于查询用户可烹饪的菜品列表，或根据输入发起对某道菜的烹饪操作。"
            "支持以下两种操作：\n"
            "1. 查询模式（action:get）：返回当前系统支持的所有菜品。\n"
            "2. 烹饪模式（action:make）：根据提供的菜品名称进行精确或模糊匹配，并发送烹饪指令。\n\n"
            f"当前支持的菜品有：{', '.join(redisClient.hkeys('recipe:nameMap'))}"
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "action": {
                    "type": "string",
                    "description": "动作名称，可选值：get(获取),make(烹饪/制作)"
                },
                "values": {
                    "type": "list",
                    "description": (
                        f"菜品名称，可选值：{redisClient.hkeys('recipe:nameMap')},匹配不到就不返回"
                        "在烹饪模式中，values 支持以下三种匹配方式：\n"
                        "- 精确匹配：完全匹配菜品名。\n"
                        "- 左模糊匹配：前缀匹配（如'蛋炒' 匹配 '蛋炒饭'）。\n"
                        "- 右模糊匹配：后缀匹配（如'茄子' 匹配 '油焖茄子',肉沫茄子'）。\n"
                        "注意：若某个菜品同时满足多个匹配条件，则视为不匹配。\n"
                        "当输入内容匹配到多个有效菜品时，系统将列出所有匹配结果供用户进一步选择。\n"
                    )
                }
            },
            "required": ["action", "values"]
        }
    }
}

async def _get_device_status(conn):
    """获取菜谱"""
    names = redisClient.hkeys("recipe:nameMap")
    if not names:
        raise Exception("您不能制作任何菜品")
    return f"当前能制作的菜肴为{','.join(names)}"

async def _make_device_property(conn, values=None):
    if not values:
        return "未匹配到任何菜肴"

    names = redisClient.hkeys("recipe:nameMap")
    if not names:
        return "菜谱中已经没有菜了"

    # 初始化匹配结果列表
    matched_devices = []

    for value in values:
        # 精确匹配
        exact_match = [e for e in names if e == value]

        if len(exact_match) > 0:
            matched_devices.extend(exact_match)
            continue
        # 左模糊匹配
        left_match = [e for e in names if (e.startswith(value) and not e.endswith(value))]
        # 右模糊匹配
        right_match = [e for e in names if (not e.startswith(value) and e.endswith(value))]

        # 合并匹配结果
        matched_devices.extend(left_match + right_match)

    if len(matched_devices)==0:
        response = f"暂不支持制作{','.join(values)}"
    elif len(matched_devices)==1:
        info_str = redisClient.hget("recipe:nameMap",matched_devices[0])
        info_dict =  json.loads(info_str)
        if isinstance(info_dict, str):
            info_dict = json.loads(info_dict)
        send_message = json.dumps({"type": "recipe", "recipe": info_dict})
        await conn.websocket.send(send_message)
        response = f"制作{matched_devices[0]}指令发送成功"
    else :
        response = f"为您匹配到{len(matched_devices)}个菜肴,分别为{','.join(matched_devices)}您要烹饪哪一个？"

    return response


def _recipe_device_action(conn, func, *args, **kwargs):
    """处理设备操作的通用函数"""
    future = asyncio.run_coroutine_threadsafe(
        func(conn, *args, **kwargs), conn.loop)
    try:
        response = future.result()
        logger.bind(tag=TAG).info(f"{response}")

        return ActionResponse(action=Action.RESPONSE, result="执行成功", response=response)
    except Exception as e:
        logger.bind(tag=TAG).error(f"{e}")
        return ActionResponse(action=Action.RESPONSE, result=None, response=f"{e}")

@register_function('recipe_device', recipe_device_function_desc, ToolType.IOT_CTL)
def recipe_device(conn, action: str, values: str = None):
    if action not in ["get", "make"]:
        raise Exception(f"未识别的动作名称: {action}")

    if action == "get":
        # get
        return _recipe_device_action(
            conn, _get_device_status,
        )
    else:
        return _recipe_device_action(
            conn, _make_device_property, values=values
        )
