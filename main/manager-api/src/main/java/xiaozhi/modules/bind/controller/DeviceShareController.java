package xiaozhi.modules.bind.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.bind.enums.DeviceTypeEnum;
import xiaozhi.modules.bind.model.dto.DeviceShareDto;
import xiaozhi.modules.bind.model.entity.DeviceShareEntity;
import xiaozhi.modules.bind.model.query.DeviceSharePageQuery;
import xiaozhi.modules.bind.model.vo.DeviceShareVo;
import xiaozhi.modules.bind.service.DeviceShareService;
import xiaozhi.modules.device.dao.DeviceDao;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.tb.entity.TbDeviceEntity;
import xiaozhi.modules.tb.service.TbDeviceService;

import java.util.List;

/**
 * xiaozhi-server tb配置获取
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("bind/deviceShare")
@Tag(name = "设备共享管理")
@AllArgsConstructor
public class DeviceShareController {
    private final DeviceShareService deviceShareService;
    private final DeviceDao aiDeviceDao;
    private final TbDeviceService tbDeviceService;

    @GetMapping("/list/{deviceId}")
    @Operation(operationId = "共享列表")
    public Result<List<DeviceShareVo>> getList(@Parameter(description = "设备id") @PathVariable String deviceId) {
        return ResultUtils.success(deviceShareService.getList(deviceId));
    }


    @PostMapping("/shareDevice")
    @Operation(summary = "共享设备")
    public Result<Void> shareDevice(@RequestBody DeviceShareDto deviceShareDto) {
        deviceShareService.shareDevice(deviceShareDto);
        return ResultUtils.success(null);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "解除共享")
    public Result<Void> delete(@PathVariable String id) {
        Long userId = SecurityUser.getUserId();
        DeviceShareEntity deviceShareEntity = deviceShareService.getById(id);
        Assert.notNull(deviceShareEntity, "共享不存在");
        if(deviceShareEntity.getUserId().equals(userId)){
            deviceShareService.removeById(id);
        }else {
            if(deviceShareEntity.getType().equals(DeviceTypeEnum.AI.getCode())){
                DeviceEntity aiDeviceEntity = aiDeviceDao.selectById(deviceShareEntity.getDeviceId());
                Assert.notNull(aiDeviceEntity, "设备不存在");
                Assert.isTrue(aiDeviceEntity.getUserId().equals(userId), "无权限");
                deviceShareService.removeById(id);
            }else if(deviceShareEntity.getType().equals(DeviceTypeEnum.TB.getCode())){
                    TbDeviceEntity tbDeviceEntity = tbDeviceService.getById(deviceShareEntity.getDeviceId());
                Assert.notNull(tbDeviceEntity, "设备不存在");
                Assert.isTrue(tbDeviceEntity.getUserId().equals(userId), "无权限");
                deviceShareService.removeById(id);
            }else {
                Assert.isTrue(false, "设备不存在");
            }
        }
        return ResultUtils.success(null);
    }



}
