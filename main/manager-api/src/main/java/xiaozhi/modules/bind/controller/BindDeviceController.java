package xiaozhi.modules.bind.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.bind.service.DeviceShareService;
import xiaozhi.modules.iot.service.IotWsService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.tb.service.TbDeviceService;

import java.util.List;

/**
 * xiaozhi-server 获取
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("bind/device")
@Tag(name = "绑定设备管理")
@AllArgsConstructor
public class BindDeviceController {
    private final IotWsService iotWsService;
    private final TbDeviceService tbDeviceService;
    private final DeviceShareService deviceShareService;

    @GetMapping("/agentList")
    @Operation(summary = "获取当前用户的所有智能体")
    public Result<List<JSONObject>> agentList(@RequestParam(required = false) String agentId) {
        List<JSONObject> agentEntityList = deviceShareService.getAgentList(agentId);
        return ResultUtils.success(agentEntityList);
    }

    @GetMapping("/list")
    @Operation(summary = "获取当前用户的所有设备")
    public Result<List<JSONObject>> list(@RequestParam(required = false) String agentId) {
        List<JSONObject> jsonObjectList = iotWsService.deviceList(agentId);
        if(StrUtil.isBlank(agentId)){
            jsonObjectList.addAll(tbDeviceService.getTbDeviceList());
        }
        return new Result<List<JSONObject>>().ok(jsonObjectList);
    }


}
