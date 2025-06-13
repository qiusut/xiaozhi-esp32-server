import json
from config.logger import setup_logging
from config.settings import redisClient
from core.utils.util import check_model_key
from core.utils.util import invoking_http_api

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

def append_devices_to_prompt(function_handler):
    try:
        if "tb_device" in function_handler.config["Intent"][function_handler.config["selected_module"]["Intent"]].get(
                "functions", []
        ):
            device_id = function_handler.conn.headers.get("device-id", "").replace(':', '-')
            user_id = redisClient.get(f"device:{device_id}:user_id")
            if user_id:
                plugin_config = function_handler.conn.config["plugins"]["tb_device"]
                init_tb_token(user_id,plugin_config) #初始化token
                tbuser = getTbUser(user_id)

                tb_device_list = getTbDevices(user_id,tbuser)

                prompt = "下面是我的设备，可以通过小智控制:"
                #if len(tb_device_list) == 0:
                    #return

                if tb_device_list:
                    tb_names = set()
                    control_device_dict = {}
                    for tb_device in tb_device_list:
                        tb_names.add(tb_device["name"])
                        control_device_list = control_device_dict.get(tb_device["type"], [])
                        control_device_list.append(tb_device)
                        control_device_dict[tb_device["type"]] = control_device_list

                    # 序列化字典中的列表为 JSON 字符串
                    control_device_dict_serialized = {k: json.dumps(v, ensure_ascii=False) for k, v in control_device_dict.items()}
                    redisClient.hmset(f"tb:user:{user_id}:control_device",control_device_dict_serialized)

                    all_devices = redisClient.lrange('tb:device', 0, -1)
                    device_type_dict = {}
                    # 遍历每个元素，解析 JSON 并构建结果字典
                    for item in all_devices:
                        try:
                            # 将 JSON 字符串反序列化为 Python 字典
                            data = json.loads(item)
                            # 检查字典中是否包含所需的键
                            if 'type' in data and 'funs' in data:
                                # 使用 'type' 作为键，'funs' 作为值
                                device_type_dict[data['type']] = data['funs']
                        except json.JSONDecodeError:
                            print(f"无法解析 JSON: {item}")

                    # 初始化功能字典
                    func_dict = {}
                    tb_name_fun = {
                        "type": "list",
                        "description": redisClient.get('tb:name_desc').format(names=tb_names)
                    }
                    # 遍历设备列表，直接构造功能字典
                    for device in tb_device_list:
                        device_type = device.get("type")
                        if device_type and device_type in device_type_dict:
                            tb_funs = device_type_dict[device_type]
                            for fun_name in tb_funs:
                                function_call = json.loads(redisClient.hget(f"tb:device_fun:{device_type}:{fun_name}", "function_call"))
                                properties = function_call["function"]["parameters"]["properties"]
                                properties["tb_name"] = tb_name_fun
                                func_dict[device_type+"_"+fun_name] = function_call

                    # 遍历功能字典，注册功能
                    for tb_key,tb_value in func_dict.items():
                        function_handler.conn.function_registry.register_tb_function(tb_key,tb_value)

                else:
                    redisClient.delete(f"tb:user:{user_id}:control_device")

                for tb_device in tb_device_list:
                    prompt += tb_device["name"] + ","
                function_handler.conn.prompt += prompt
                """
                "," + tb_device["id"]["id"] + 
                
                prompt内容：'下面是我家智能设备，可以通过thingsboard控制
                客厅,玩具灯,switch.cuco_cn_460494544_cp1_on_p_2_1
                卧室,台灯,switch.iot_cn_831898993_socn1_on_p_2_1
                '
                """
                # 更新提示词
                function_handler.conn.dialogue.update_system_message(function_handler.conn.prompt)

    except Exception as e:
        logger.bind(tag=TAG).error(f"tb初始化组件失败: {e}")


def initialize_tb_handler(conn):
    global TB_CACHE
    if TB_CACHE == {}:
        if conn.use_function_call_mode:
            funcs = conn.config["Intent"]["function_call"].get("functions", [])
            if "tb_device" in funcs:
                TB_CACHE['base_url'] = conn.config["plugins"]["home_assistant"].get("base_url")
                TB_CACHE['api_key'] = conn.config["plugins"]["home_assistant"].get("api_key")

                check_model_key("home_assistant", TB_CACHE['api_key'])
    return TB_CACHE

#初始化tb系统token缓存
def init_tb_token(user_id,plugin_config):
    tb_token = None
    if user_id:
        key_prefix = "tb:user:"+user_id
        tb_token = redisClient.get(key_prefix+":token")
        if not tb_token:
            invoking_api = {
                "url": f"{redisClient.get('tb:url')}/{loginUrl}",
                "method": "POST",
                "body": {
                    "username": plugin_config.get("tb_username"),
                    "password": plugin_config.get("tb_password")
                }
            }
            response = invoking_http_api(invoking_api)
            response_dict = json.loads(response.text)
            redisClient.set(key_prefix+":token", response_dict["token"])
            redisClient.expire(key_prefix + ":token", 1800)
            tb_token = response_dict["token"]
    return tb_token

#获取tb用户
def getTbUser(user_id):
    key_prefix = "tb:user:"+user_id
    invoking_api = {
        "url": f"{redisClient.get('tb:url')}/{userUrl}",
        "headers": {
            "Authorization": "Bearer "+redisClient.get(key_prefix+":token")
        }
    }
    response = invoking_http_api(invoking_api)
    return json.loads(response.text)

#获取tb设备
def getTbDevices(user_id,tbuser:dict,**kwargs):
    key_prefix = "tb:user:"+user_id
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

