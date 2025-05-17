package xiaozhi.modules.iot.service;

import xiaozhi.modules.iot.dto.CommandDTO;

public interface IotWsService {

    String getWs(String device_mac);

    public String sendCommand(CommandDTO dto);
}