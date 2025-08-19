package xiaozhi.modules.tb.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.tb.dto.TbDeviceDTO;
import xiaozhi.modules.tb.dto.TbDeviceRpcDTO;
import xiaozhi.modules.tb.entity.TbDeviceEntity;
import xiaozhi.modules.tb.query.DeviceQueryPage;
import xiaozhi.modules.tb.service.TbDeviceService;
import xiaozhi.modules.tb.vo.TbDeviceVO;

/**
 * xiaozhi-server tb配置获取
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("tb/device")
@Tag(name = "TB设备管理")
@AllArgsConstructor
public class TbDeviceController {
    private final TbDeviceService tbDeviceService;

    @GetMapping("/page")
    @Operation(operationId = "分页查询")
    public Result<PageData<TbDeviceVO>> getPage(@ParameterObject DeviceQueryPage deviceQueryPage) {

        Page<TbDeviceEntity> page = tbDeviceService.getPage(deviceQueryPage);
        return new Result<PageData<TbDeviceVO>>().ok(new PageData<TbDeviceVO>(BeanUtil.copyToList(page.getRecords(), TbDeviceVO.class),  page.getTotal()));
    }

    @GetMapping("{id}")
    @Operation(operationId = "设备信息")
    public Result<TbDeviceVO> get(@PathVariable("id") String id) {
        TbDeviceVO tbDevice = tbDeviceService.getTbDevice(id);
        return new Result<TbDeviceVO>().ok(tbDevice);
    }

    @GetMapping("getTbDeviceByTbId/{id}")
    @Operation(operationId = "获取tb设备信息")
    public Result<JSONObject> getTbDeviceByTbId(@PathVariable("id") String id) {
        return new Result<JSONObject>().ok(tbDeviceService.getTbDeviceJson(id));
    }

    @PostMapping("/addTbDevice")
    @Operation(summary = "绑定")
    @RequiresPermissions("tb:device:addTbDevice")
    public Result<Void> addTbDevice(@RequestBody TbDeviceDTO tbDeviceDTO) {
        tbDeviceService.addTbDevice(tbDeviceDTO);
        return new Result<>();
    }

    @PutMapping("/updateName")
    @Operation(summary = "修改名称")
    public Result<Void> updateName(@RequestBody JSONObject jsonObject) {
        String id = jsonObject.getStr("id");
        String name = jsonObject.getStr("name");
        tbDeviceService.update(Wrappers.lambdaUpdate(TbDeviceEntity.class).set(TbDeviceEntity::getName,name).eq(TbDeviceEntity::getId,id));
        tbDeviceService.initTbDeviceRedis(SecurityUser.getUserId());
        return new Result<>();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "解绑/删除")
    @RequiresPermissions("tb:device:unbind")
    public Result<Void> unbind(@PathVariable String id) {
        tbDeviceService.deleteTbDevice(id);
        return new Result<>();
    }

    @PostMapping("/sendDeviceRpc")
    @Operation(summary = "控制Rpc设备")
    public Result<String> sendDeviceRpc(@RequestBody TbDeviceRpcDTO TbDeviceRpcDTO) {
        return new Result<String>().ok(tbDeviceService.sendDeviceRpc(TbDeviceRpcDTO));
    }

}
