package xiaozhi.modules.iot.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import xiaozhi.common.user.UserDetail;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.bind.model.entity.DeviceShareEntity;
import xiaozhi.modules.bind.service.DeviceShareService;
import xiaozhi.modules.device.dao.DeviceDao;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.iot.dto.CommandDTO;
import xiaozhi.modules.iot.service.IotWsService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.service.SysParamsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IotWsServiceImpl implements IotWsService {

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    private DeviceShareService deviceShareService;

    @Resource
    private AgentDao agentDao;

    @Resource
    private DeviceDao deviceDao;

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
    public List<JSONObject> deviceList(String agentId) {
        UserDetail user = SecurityUser.getUser();
        List<JSONObject> jsonObjectList = new ArrayList<>();
        List<AgentEntity> agents = agentDao.selectList(Wrappers.lambdaQuery(AgentEntity.class)
                .eq(StrUtil.isNotBlank(agentId), AgentEntity::getId, agentId)
                .eq(AgentEntity::getUserId, user.getId())
        );
        if (CollectionUtil.isNotEmpty(agents)) {
            Map<String, String> nameMap = agents.stream().collect(Collectors.toMap(AgentEntity::getId, AgentEntity::getAgentName));

            List<DeviceShareEntity> deviceShareList = deviceShareService.list(Wrappers.lambdaQuery(DeviceShareEntity.class)
                    .eq(DeviceShareEntity::getUserId, user.getId())
            );
            List<String> deviceIds = new ArrayList<>();
            if(CollUtil.isNotEmpty(deviceShareList)){
                deviceIds = deviceShareList.stream().map(DeviceShareEntity::getDeviceId).toList();
            }

            LambdaQueryWrapper<DeviceEntity> queryWrapper = Wrappers.lambdaQuery();
            List<String> finalDeviceIds = deviceIds;
            queryWrapper.or(i -> i.and(j -> j.eq(DeviceEntity::getUserId, user.getId()).in(DeviceEntity::getAgentId, nameMap.keySet()))
                    .in(DeviceEntity::getId, finalDeviceIds)
            );
            queryWrapper.orderByAsc(DeviceEntity::getAgentId, DeviceEntity::getSort);
            List<DeviceEntity> agentList = deviceDao.selectList(queryWrapper);

            if (CollectionUtil.isNotEmpty(agentList)) {
                String result = this.getWs(null);
                JSONObject result_json = JSONUtil.parseObj(result);
                agentList.forEach(e -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject = JSONUtil.parseObj(e);
                    jsonObject.set("agentName", nameMap.get(e.getAgentId()));
                    jsonObject.set("isActive", result_json.containsKey(e.getMacAddress()) ? 1 : 0);
                    jsonObject.set("isShare", finalDeviceIds.contains(e.getId()));
                    jsonObject.set("iot", result_json.get(e.getMacAddress()));
                    jsonObjectList.add(jsonObject);
                });
            }
        }
        return jsonObjectList;
    }

    @Override
    public String getWs(String device_mac){
        String result = JSONUtil.toJsonStr(new JSONObject());
        String http_url = sysParamsService.getValue("server.http_url", true);
        String http_url_ws = sysParamsService.getValue("server.http_url_ws", true);
        if(StrUtil.equals("dev", profiles_active)){
            http_url = "http://127.0.0.1:8003";
        }
        Map<String, Object> headers = new HashMap<>();
        if(StrUtil.isNotBlank(device_mac)){
            headers.put("device_mac", device_mac);
        }
        try {
            result = HttpUtil.get(http_url + http_url_ws,headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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