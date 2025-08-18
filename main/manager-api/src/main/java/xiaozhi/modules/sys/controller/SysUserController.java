package xiaozhi.modules.sys.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.query.SysUserQuery;
import xiaozhi.modules.sys.service.SysRoleService;
import xiaozhi.modules.sys.service.SysUserPlusService;
import xiaozhi.modules.sys.service.SysUserRoleService;
import xiaozhi.modules.sys.vo.SysUserVO;
import xiaozhi.modules.tb.entity.TbDeviceEntity;
import xiaozhi.modules.tb.service.TbDeviceService;

import java.util.List;

/**
 * 用户控制层
 */
@AllArgsConstructor
@RestController
@RequestMapping("/admin/user")
@Tag(name = "用户管理")
public class SysUserController {
    private final SysUserPlusService sysUserPlusService;
    private final SysUserRoleService sysUserRoleService;
    private final DeviceService deviceService;
    private final TbDeviceService tbDeviceService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<PageData<SysUserVO>> page(@ParameterObject @Valid SysUserQuery query) {
        PageData<SysUserVO> page = sysUserPlusService.page(query);
        return Result.okResult(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "用户信息详情")
    //@RequiresPermissions("sys:role:info")
    public Result<SysUserVO> get(@PathVariable("id") Long id) {
        SysUserEntity entity = sysUserPlusService.getById(id);

        // 转换对象
        SysUserVO vo = BeanUtil.copyProperties(entity, SysUserVO.class);

        Long deviceCount = deviceService.selectCountByUserId(entity.getId());
        if(deviceCount!=null){
            vo.setDeviceCount(Math.toIntExact(deviceCount));
        }

        long tbDeviceCount = tbDeviceService.count(Wrappers.lambdaQuery(TbDeviceEntity.class).eq(TbDeviceEntity::getCreator, entity.getId()));
        vo.setTbDeviceCount(Math.toIntExact(tbDeviceCount));


        List<Long> roleIdList = sysUserRoleService.getRoleIdList(id);
        // 查询角色对应的菜单
        vo.setRoleIdList(roleIdList);

        return Result.okResult(vo);
    }

    @PostMapping("role/{userId}")
    @Operation(summary = "给用户分配角色")
    @LogOperation("给用户分配角色")
    @RequiresPermissions("sys:role:superAdmin")
    public Result<String> userSave(@PathVariable("userId") Long userId, @RequestBody List<Long> roleList) {
        Assert.isTrue(CollUtil.isNotEmpty(roleList), "roledList is empty!");
        SysUserEntity entity = sysUserPlusService.getById(userId);
        Assert.isTrue(entity != null, "用户不存在!");

        sysUserRoleService.saveOrUpdate(userId, roleList);

        return Result.okResult(null);
    }


}
