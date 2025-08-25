package xiaozhi.modules.bind.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.bind.model.dto.DeviceShareDto;
import xiaozhi.modules.bind.model.entity.DeviceShareEntity;
import xiaozhi.modules.bind.model.vo.DeviceShareVo;

import java.util.List;

public interface DeviceShareService extends IService<DeviceShareEntity> {

    List<DeviceShareVo> getList(String deviceId);

    public  void shareDevice(DeviceShareDto deviceShareDto);

    List<JSONObject> getAgentList(String agentId);
}