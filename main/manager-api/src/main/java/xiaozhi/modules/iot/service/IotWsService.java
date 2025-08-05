package xiaozhi.modules.iot.service;

import cn.hutool.json.JSONObject;
import xiaozhi.modules.iot.dto.CommandDTO;

import java.util.List;

public interface IotWsService {

    List<JSONObject> deviceList(String agentId);

    String getWs(String device_mac);

    public String sendCommand(CommandDTO dto);
}