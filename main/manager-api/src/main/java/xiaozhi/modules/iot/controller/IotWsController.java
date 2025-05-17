package xiaozhi.modules.iot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.device.dao.DeviceDao;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.iot.dto.CommandDTO;
import xiaozhi.modules.iot.service.IotWsService;
import xiaozhi.modules.security.user.SecurityUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * iot,websocket管理
 */
@RestController
@RequestMapping("/iot/ws")
@Tag(name = "iot,websocket管理")
@AllArgsConstructor
public class IotWsController {
    private final IotWsService iotWsService;
    private final AgentDao agentDao;
    private final DeviceDao deviceDao;

    @GetMapping("/deviceList")
    @Operation(summary = "获取当前用户的所有设备")
    public Result<List<JSONObject>> deviceList(@RequestParam(required = false) String agentId) {
        UserDetail user = SecurityUser.getUser();
        List<JSONObject> jsonObjectList = new ArrayList<>();
        List<AgentEntity> agents = agentDao.selectList(Wrappers.lambdaQuery(AgentEntity.class)
                .eq(StrUtil.isNotBlank(agentId),AgentEntity::getId, agentId)
                .eq(AgentEntity::getUserId, user.getId())
        );
        if(CollectionUtil.isNotEmpty(agents)){
            Map<String, String> nameMap = agents.stream().collect(Collectors.toMap(AgentEntity::getId, AgentEntity::getAgentName));
            List<DeviceEntity> agentList = deviceDao.selectList(Wrappers.lambdaQuery(DeviceEntity.class)
                .eq(DeviceEntity::getUserId, user.getId())
                .in(DeviceEntity::getAgentId, nameMap.keySet())
                .orderByAsc(DeviceEntity::getAgentId,  DeviceEntity::getSort)
            );

            if(CollectionUtil.isNotEmpty(agentList)){
                String result = iotWsService.getWs(null);
                JSONObject result_json = JSONUtil.parseObj(result);
                agentList.forEach(e->{
                    JSONObject jsonObject = new JSONObject();
                    jsonObject = JSONUtil.parseObj(e);
                    jsonObject.set("agentName", nameMap.get(e.getAgentId()));
                    jsonObject.set("isActive", result_json.containsKey(e.getMacAddress())? 1:0);
                    jsonObject.set("iot",result_json.get(e.getMacAddress()));
                    jsonObjectList.add(jsonObject);
                });
            }

        }
        return new Result<List<JSONObject>>().ok(jsonObjectList);
    }

    @GetMapping("/getOnMac")
    @Operation(summary = "获取当前所有正在连接列表（返回的mac就是在线的mac）")
    public Result<JSONObject> getOnMac() {
        String result = iotWsService.getWs(null);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        return new Result<JSONObject>().ok(jsonObject);
    }

    @GetMapping("{device_mac}")
    @Operation(summary = "单个连接信息")
    public Result<JSONObject> get(@PathVariable("device_mac") String device_mac) {
        String result = iotWsService.getWs(device_mac);
        JSONObject jsonObject = JSONUtil.parseObj(result);
        return new Result<JSONObject>().ok(jsonObject.getJSONObject(device_mac));
    }

    @PostMapping
    @Operation(summary = "发送指令")
    @LogOperation("发送指令")
    public Result<String> sendCommand(@RequestBody @Valid CommandDTO dto) {
        return new Result<String>().ok(iotWsService.sendCommand(dto));
    }


}
