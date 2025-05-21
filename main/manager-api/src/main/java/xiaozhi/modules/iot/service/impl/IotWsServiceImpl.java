package xiaozhi.modules.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xiaozhi.modules.iot.dto.CommandDTO;
import xiaozhi.modules.iot.service.IotWsService;
import xiaozhi.modules.sys.service.SysParamsService;

import java.util.HashMap;
import java.util.Map;

@Service
public class IotWsServiceImpl implements IotWsService {

    @Resource
    private SysParamsService sysParamsService;

    @Value("${spring.profiles.active}")
    private String profiles_active;

    private static final String COMMAND_TEMPLATE = """
        {
        "type": "iot",
        "commands": [
            {
                "name": "${name}",
                "method": "${method}",
                "parameters": ${parameters}
            }
        ]
    }
    """;

    @Override
    public String getWs(String device_mac){
        String http_url = sysParamsService.getValue("server.http_url", true);
        String http_url_ws = sysParamsService.getValue("server.http_url_ws", true);
        if(StrUtil.equals("dev", profiles_active)){
            http_url = "http://127.0.0.1:8003";
        }
        Map<String, Object> headers = new HashMap<>();
        if(StrUtil.isNotBlank(device_mac)){
            headers.put("device_mac", device_mac);
        }
        return HttpUtil.get(http_url + http_url_ws,headers);
    }

    @Override
    public String sendCommand(CommandDTO dto){
        String http_url = sysParamsService.getValue("server.http_url", true);
        String http_url_ws = sysParamsService.getValue("server.http_url_ws", true);
        if(StrUtil.equals("dev", profiles_active)){
            http_url = "http://127.0.0.1:8003";
        }
        Map<String, String> headers = new HashMap<>();
        if(StrUtil.isNotBlank(dto.getDevice_mac())){
            headers.put("device_mac", dto.getDevice_mac());
        }

        JSONObject command = new JSONObject();
        command.set("name",dto.getName());
        command.set("method",dto.getMethod());
        if(dto.getParameters() != null){
            command.set("parameters",dto.getParameters());
        }

        // 发起 POST 请求
        return HttpUtil.createRequest(Method.POST, http_url + http_url_ws)
                .addHeaders(headers)
                .body("""
                        {"type": "iot","commands": [${command}]}""".replace("${command}",command.toString()))
                .execute().body();
    }


}