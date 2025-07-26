import asyncio
import json
import threading

from config.logger import setup_logging
from config.settings import redisClient
from plugins_func.register import register_function, ToolType, ActionResponse, Action

TAG = __name__
logger = setup_logging()

pvt_recipe_pre = "$u$"
pvt_recipe_pre_zh = "私房菜"

def get_recipe_names(self):
    names = redisClient.hkeys("recipe:nameMap")
    device_id = self.headers.get("device-id", "")
    private_name  = redisClient.hkeys("device:"+device_id+":nameMap", device_id)
    for name in private_name:
        if name in names:
            names.append(pvt_recipe_pre_zh+name)
        else:
            names.append(name)
    return names


# 设备控制
recipe_device_function_desc = {
    "type": "function",
    "function": {
        "name": "recipe_device",
        "description": (
            "用于查询用户可烹饪的菜品列表，以及用户想要发起对某道菜的烹饪操作，\n"
            "用户想要做/炒/蒸/煮/烧/焖某道菜，或因为assistant的询问直接指定名称"
            "支持以下两种操作：\n"
            "1. 查询模式（action:get）：返回当前系统支持的所有菜品。\n"
            "2. 烹饪模式（action:make）：根据提供的菜品名称进行精确或模糊匹配，返回匹配到的菜品名称,比如用户说做个炒饭/抄个饭/做个。\n"
            "用户可进行多轮选择，如用户发送做个炒饭,系统匹配到蛋炒饭,杭椒炒饭返回，用户可以指定第一个或第二个进行再次匹配，匹配到就返回对应的菜品名称"
            f"当前支持的菜品有：{', '.join(redisClient.hkeys('recipe:nameMap'))}\n"
            "注意:当用户指定第一个或第二个这样类似的指令时要判断用户是否是在指定菜品，如果是在指定菜品就返回对话中对应的菜品名称\n"
            "特别注意:禁止偷懒,每次都需要匹配这个方法\n"
        ),
        "parameters": {
            "type": "object",
            "properties": {
                "action": {
                    "type": "string",
                    "description": "动作名称，可选值：get(获取),make(烹饪/制作)"
                },
                "values": {
                    "type": "array",
                    "description": (
                        f"菜品名称，可选值：{redisClient.hkeys('recipe:nameMap')},匹配不到就不返回"
                        "在烹饪模式中，values 支持以下三种匹配方式：\n"
                        "- 精确匹配：完全匹配菜品名。\n"
                        "- 左模糊匹配：前缀匹配（如'蛋炒' 匹配 '蛋炒饭'）。\n"
                        "- 右模糊匹配：后缀匹配（如'茄子' 匹配 '油焖茄子',肉沫茄子'）。\n"
                        "注意：若某个菜品同时满足多个匹配条件，则视为不匹配。\n"
                        "当输入内容匹配到多个有效菜品时，系统将列出所有匹配结果供用户进一步选择。\n"
                    )
                },
                "isChoice": {
                    "type": "boolean",
                    "description": "默认为false,当上一句是assistant询问用户要烹饪哪一个，用户回复指定第一个或第二个或直接指定菜名时为true"
                }
            },
            "required": ["action", "values"]
        }
    }
}

async def _get_device_status(conn):
    """获取菜谱"""
    names = redisClient.hkeys("recipe:nameMap")
    device_id = conn.headers.get("device-id", "")
    user_id = redisClient.get(f"device:{device_id.replace(':', '-')}:user_id")
    private_name  = redisClient.hkeys(f"recipe:user:{user_id}:nameMap")
    for name in private_name:
        if name in names:
            names.append(pvt_recipe_pre_zh+name)
        else:
            names.append(name)
    if not names:
        raise Exception("您不能制作任何菜品")
    return f"当前能制作的菜肴为{','.join(names)}"

async def _make_device_property(conn, values=None,isChoice=False):
    if not values:
        return "未匹配到任何菜肴"

    print(f"收到的指令为：values={values}，isChoice={isChoice}")
    names = redisClient.hkeys("recipe:nameMap")

    device_id = conn.headers.get("device-id", "")
    user_id = redisClient.get(f"device:{device_id.replace(':', '-')}:user_id")
    private_names  = redisClient.hkeys(f"recipe:user:{user_id}:nameMap")

    if not any([names, private_names]):
        return "您不能制作任何菜品"

    # 初始化匹配结果列表
    matched_devices = []

    for value in values:
        # 精确匹配
        exact_match = list()
        if names:
            exact_match.extend([e for e in names if e == value])

        if len(exact_match) > 0:
            matched_devices.extend(exact_match)
            if isChoice:
                continue

        #私房菜匹配
        private_exact_match = list()
        if private_names:
            # 精确匹配
            for e in private_names:
                if e == value or pvt_recipe_pre_zh + e == value:
                    private_exact_match.append(f"{pvt_recipe_pre+e}")


        if len(exact_match) > 0 or len(private_exact_match) > 0:
            matched_devices.extend(private_exact_match)
            continue

        # 左模糊匹配
        left_match = list()
        if names:
            left_match.extend([e for e in names if e.startswith(value)])
        #私房菜匹配
        if private_names:
            for e in private_names:
                if e.startswith(value) or (pvt_recipe_pre_zh+e).startswith(value):
                    left_match.append(f"{pvt_recipe_pre+e}")

        # 右模糊匹配
        right_match = list()
        if names:
            right_match.extend([e for e in names if e.endswith(value)])
        #私房菜匹配
        if private_names:
            for e in private_names:
                if e.endswith(value):
                    right_match.append(f"{pvt_recipe_pre+e}")

        # 合并匹配结果
        matched_devices.extend(left_match + right_match)


    if len(matched_devices)==0:
        response = f"暂不支持制作{','.join(values)}"
    elif len(matched_devices)==1:
        matched_recipe = matched_devices[0]
        if matched_recipe.startswith(pvt_recipe_pre):
            info_str = redisClient.hget(f"recipe:user:{user_id}:nameMap",matched_recipe.replace(pvt_recipe_pre,""))
        else:
            info_str = redisClient.hget("recipe:nameMap",matched_recipe)
        info_dict = json.loads(info_str)
        if isinstance(info_dict, str):
            info_dict = json.loads(info_dict)
        send_message = json.dumps({"type": "recipe", "recipe": info_dict})
        await conn.websocket.send(send_message)
        #response = None
        response = f"制作{matched_recipe.replace(pvt_recipe_pre,pvt_recipe_pre_zh)}指令发送成功"
    else :
        ret_names = str()
        for matched_recipe in matched_devices:
            if ret_names:
                ret_names+=","
            if matched_recipe.startswith(pvt_recipe_pre):
                matched_recipe = matched_recipe.replace(pvt_recipe_pre,"")
                if matched_recipe in matched_devices:
                    matched_recipe = pvt_recipe_pre_zh+matched_recipe
            ret_names+=matched_recipe

        response = f"为您匹配到{len(matched_devices)}个菜肴,分别为{ret_names}您要烹饪哪一个？"

    return response


def _recipe_device_action(conn, func, *args, **kwargs):

    # 创建新的事件循环
    new_loop = asyncio.new_event_loop()
    # 在新线程中运行事件循环（仅运行一次）
    loop_thread = threading.Thread(target=new_loop.run_forever, daemon=True)
    loop_thread.start()

    action=Action.REQLLM
    result=None
    response=None
    """处理设备操作的通用函数"""
    future = asyncio.run_coroutine_threadsafe(func(conn, *args, **kwargs), new_loop)

    try:
        result = future.result()
        if result:
            logger.bind(tag=TAG).info(f"{result}")
    except Exception as e:
        logger.bind(tag=TAG).error(f"{e}")
        action = Action.RESPONSE
        response = f"{e}"
    finally:
        new_loop.call_soon_threadsafe(new_loop.stop)  # 停止事件循环
        loop_thread.join()  # 等待线程结束
        new_loop.close()
    return ActionResponse(action=action, result=result, response=response)

@register_function('recipe_device', recipe_device_function_desc, ToolType.SYSTEM_CTL)
def recipe_device(conn, action: str, values: str = None, isChoice: bool = False):
    logger.bind(tag=TAG).info(f"成功进入了菜谱功能recipe_device方法")
    if action not in ["get", "make"]:
        raise Exception(f"未识别的动作名称: {action}")

    if action == "get":
        # get
        return _recipe_device_action(
            conn, _get_device_status,
        )
    else:
        return _recipe_device_action(
            conn, _make_device_property, values=values, isChoice=isChoice
        )
