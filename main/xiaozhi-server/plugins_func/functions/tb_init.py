import copy
import json

from config.logger import setup_logging
from config.settings import redisClient
from core.utils.util import check_model_key
from core.utils.util import invoking_http_api
from plugins_func.register import all_function_registry

TAG = __name__
logger = setup_logging()

TB_CACHE = {}

SYS_ADMIN = 'SYS_ADMIN'
TENANT_ADMIN = 'TENANT_ADMIN'
CUSTOMER_USER = 'CUSTOMER_USER'

userUrl = "api/auth/user"
loginUrl = "api/auth/login"
getCustomerDeviceInfos = "api/customer/{customer_id}/deviceInfos"
getTenantDeviceInfos = "api/tenant/deviceInfos"

def append_devices_to_prompt(conn):
    try:
        if "tb_device" in conn.config["Intent"][conn.config["selected_module"]["Intent"]].get(
                "functions", []
        ):

            device_id = conn.headers.get("device-id", "").replace(':', '-')
            user_id = redisClient.get(f"device:{device_id}:user_id")
            if user_id:

                user_key = f"tb:user:{user_id}:"
                fun_call = redisClient.hgetall(user_key+"fun_call")

                if fun_call:
                    tb_names = {json.loads(value)["name"] for key, value in fun_call.items()}

                    # 初始化功能字典
                    func_dict = {}
                    tb_name_fun = {
                        "type": "array",
                        "description": redisClient.get('tb:name_desc').format(names=tb_names)
                    }

                    for key, value in fun_call.items():
                        tb_value = json.loads(value)
                        tb_type = tb_value["type"]
                        tb_name = tb_value["name"]
                        prefix = f"tb:device_fun:{tb_type}:"
                        for method_key in redisClient.scan_iter(match=prefix+"*", count=100):
                            method_key_prefix = method_key.removeprefix(prefix)  # 只替换第一次出现的前缀
                            redis_function_call = redisClient.hget(method_key, "function_call")
                            function_call = json.loads(redis_function_call
                                                       .replace("{{name}}", tb_name)
                                                       .replace(f"tb_{tb_type}_{method_key_prefix}",f"tb_{method_key_prefix}_{key}")
                                                       )
                            properties = function_call["function"]["parameters"]["properties"]
                            properties["tb_name"] = tb_name_fun
                            func_dict["tb_"+method_key_prefix+"_"+key] = function_call

                    # 遍历功能字典，注册功能
                    func = all_function_registry.get("tb_device")
                    if func:
                        for tb_key,tb_value in func_dict.items():
                            func_copy = copy.copy(func)
                            func_copy.name = tb_key
                            func_copy.description = tb_value
                            all_function_registry[tb_key] = func_copy
                            """
                            all_tools[tb_key] = ToolDefinition(
                                name=tb_key,
                                description=tb_value,
                                tool_type=ToolType.SERVER_PLUGIN,
                            )
                            """

    except Exception as e:
        logger.bind(tag=TAG).error(f"tb初始化组件失败: {e}")


#初始化tb系统token缓存
def init_tb_token(tb_username,tb_password):
    tb_token = None
    if tb_username:
        key_prefix = f"tb:account:{tb_username}"
        tb_token = redisClient.get(key_prefix+":token")
        if not tb_token:
            invoking_api = {
                "url": f"{redisClient.get('tb:url')}/{loginUrl}",
                "method": "POST",
                "body": {
                    "username": tb_username,
                    "password": tb_password
                }
            }
            response = invoking_http_api(invoking_api)
            response_dict = json.loads(response.text)
            redisClient.set(key_prefix+":token", response_dict["token"])
            redisClient.expire(key_prefix + ":token", 1800)
            tb_token = response_dict["token"]
    return tb_token

#获取tb用户
def getTbUser(tb_username):
    key_prefix = f"tb:account:{tb_username}"
    invoking_api = {
        "url": f"{redisClient.get('tb:url')}/{userUrl}",
        "headers": {
            "Authorization": "Bearer "+redisClient.get(key_prefix+":token")
        }
    }
    response = invoking_http_api(invoking_api)
    return json.loads(response.text)

#获取tb设备
def getTbDevices(tb_username,tbuser:dict,**kwargs):
    key_prefix = f"tb:account:{tb_username}"
    if tbuser["authority"] == CUSTOMER_USER:
        customer_id = tbuser["customerId"]["id"]
        url = f"{redisClient.get('tb:url')}/{getCustomerDeviceInfos}?active=true&page=0&pageSize=50"
        url = url.format(customer_id=customer_id)
    elif tbuser["authority"] == TENANT_ADMIN:
        url = f"{redisClient.get('tb:url')}/{getTenantDeviceInfos}?active=true&page=0&pageSize=50"
    else:
        return None


    for key,value in kwargs.items():
        url += "&"+key+"="+value

    invoking_api = {
        "url": url,
        "headers": {
            "Authorization": "Bearer "+redisClient.get(key_prefix+":token")
        }
    }

    response = invoking_http_api(invoking_api)
    if response.status_code == 200:
        return json.loads(response.text)["data"]

