package xiaozhi.modules.iot.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.iot.dto.CommandDTO;
import xiaozhi.modules.iot.service.IotWsService;
import xiaozhi.modules.tb.service.TbDeviceService;

import java.util.List;

/**
 * iot,websocket管理
 */
@RestController
@RequestMapping("/iot/ws")
@Tag(name = "iot,websocket管理")
@AllArgsConstructor
public class IotWsController {
    private final IotWsService iotWsService;
    //private final TbDeviceService tbDeviceService;

    @GetMapping("/deviceList")
    @Operation(summary = "获取当前用户的所有设备")
    public Result<List<JSONObject>> deviceList(@RequestParam(required = false) String agentId) {
        List<JSONObject> jsonObjectList = iotWsService.deviceList(agentId);
        //jsonObjectList.addAll(tbDeviceService.getTbDeviceList());
        return new Result<List<JSONObject>>().ok(jsonObjectList);
    }

    @GetMapping("/getOnMac")
    @Operation(summary = "获取当前所有正在连接列表（返回的mac就是在线的mac）")
    @RequiresPermissions("sys:role:superAdmin")
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
