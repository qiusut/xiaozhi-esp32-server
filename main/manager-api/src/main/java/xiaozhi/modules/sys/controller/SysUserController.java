package xiaozhi.modules.sys.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.PageData;
import xiaozhi.common.page.TokenDTO;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.HttpContextUtils;
import xiaozhi.common.utils.JwtUtil;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.common.validator.ValidatorUtils;
import xiaozhi.modules.device.service.DeviceService;
import xiaozhi.modules.security.dto.SmsVerificationDTO;
import xiaozhi.modules.security.service.CaptchaService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.dto.AppUserDTO;
import xiaozhi.modules.sys.dto.SysDictTypeDTO;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.query.SysUserQuery;
import xiaozhi.modules.sys.service.SysRoleService;
import xiaozhi.modules.sys.service.SysUserPlusService;
import xiaozhi.modules.sys.service.SysUserRoleService;
import xiaozhi.modules.sys.vo.SysUserVO;
import xiaozhi.modules.tb.entity.TbDeviceEntity;
import xiaozhi.modules.tb.service.TbDeviceService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    private final CaptchaService captchaService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("admin:user:list")
    public Result<PageData<SysUserVO>> page(@ParameterObject @Valid SysUserQuery query) {
        PageData<SysUserVO> page = sysUserPlusService.page(query);
        return ResultUtils.success(page);
    }

    @GetMapping("/getUserView")
    @Operation(summary = "用户信息详情")
    //@RequiresPermissions("sys:role:info")
    public Result<SysUserVO> get(@RequestParam(required = false) Long id) {
        if(id==null){
            id = SecurityUser.getUserId();
        }

        SysUserEntity entity = sysUserPlusService.getById(id);

        // 转换对象
        SysUserVO vo = BeanUtil.copyProperties(entity, SysUserVO.class);

        Long deviceCount = deviceService.selectCountByUserId(entity.getId());
        if(deviceCount!=null){
            vo.setDeviceCount(Math.toIntExact(deviceCount));
        }

        long tbDeviceCount = tbDeviceService.count(Wrappers.lambdaQuery(TbDeviceEntity.class).eq(TbDeviceEntity::getUserId, entity.getId()));
        vo.setTbDeviceCount(Math.toIntExact(tbDeviceCount));


        List<Long> roleIdList = sysUserRoleService.getRoleIdList(id);
        // 查询角色对应的菜单
        vo.setRoleIdList(roleIdList);

        return ResultUtils.success(vo);
    }

    @PostMapping("role/{userId}")
    @Operation(summary = "给用户分配角色")
    @LogOperation("给用户分配角色")
    @RequiresPermissions("admin:user:role:assign")
    public Result<String> assignRole(@PathVariable("userId") Long userId, @RequestBody List<Long> roleList) {
        Assert.isTrue(CollUtil.isNotEmpty(roleList), "roledList is empty!");
        SysUserEntity entity = sysUserPlusService.getById(userId);
        Assert.isTrue(entity != null, "用户不存在!");

        sysUserRoleService.saveOrUpdate(userId, roleList);

        return ResultUtils.success(null);
    }

    @GetMapping("/sendSmsCaptcha")
    @Operation(summary = "发送自身短信验证码")
    public Result<Void> sendSmsCaptcha() {
        UserDetail user = SecurityUser.getUser();
        Assert.isTrue(StrUtil.isNotBlank(user.getMobile()), "该账号未绑定手机号!");
        // 发送短信验证码
        captchaService.sendSMSValidateCode(user.getMobile());
        return new Result<>();
    }

    @GetMapping("/validCaptcha")
    @Operation(summary = "校验自身验证码")
    public Result<Boolean> captchaLogin(@RequestParam String captcha) {
        UserDetail user = SecurityUser.getUser();
        boolean valid = captchaService.validateSMSValidateCode(user.getMobile(), captcha, false);
        return ResultUtils.success(valid);
    }

    @GetMapping("/sendNewMobileSms")
    @Operation(summary = "发送新手机号验证码")
    public Result<Void> sendNewMobileSms(@RequestParam String mobile) {
        UserDetail user = SecurityUser.getUser();
        Assert.isFalse(ObjectUtil.equals(mobile, user.getMobile()), "手机号不能与原手机号相同!");
        boolean exists = sysUserPlusService.exists(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getMobile, mobile));
        Assert.isFalse(exists, "此手机号码已注册!");
        // 发送短信验证码
        captchaService.sendSMSValidateCode(mobile);

        return ResultUtils.empty();
    }

    @GetMapping("/bindMobile")
    @Operation(summary = "绑定新手机号")
    public Result<Void> bindMobile(@RequestParam String mobile,
                                        @RequestParam String captcha) {
        UserDetail user = SecurityUser.getUser();
        Assert.isFalse(ObjectUtil.equals(mobile, user.getMobile()), "手机号不能与原手机号相同!");
        boolean valid = captchaService.validateSMSValidateCode(mobile, captcha, false);
        Assert.isTrue(valid, "验证码错误!");
        boolean exists = sysUserPlusService.exists(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getMobile, mobile));
        Assert.isFalse(exists, "此手机号码已被绑定!");
        sysUserPlusService.update(Wrappers.lambdaUpdate(SysUserEntity.class)
                .set(SysUserEntity::getMobile, mobile)
                .eq(SysUserEntity::getId, user.getId()));

        return ResultUtils.empty();
    }

}
