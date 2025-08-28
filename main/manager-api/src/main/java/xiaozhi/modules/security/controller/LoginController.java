package xiaozhi.modules.security.controller;

import java.io.IOException;
import java.util.*;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import xiaozhi.common.annotation.RateLimit;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.exception.RenException;
import xiaozhi.common.page.TokenDTO;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.HttpContextUtils;
import xiaozhi.common.utils.JwtUtil;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.common.validator.AssertUtils;
import xiaozhi.common.validator.ValidatorUtils;
import xiaozhi.modules.security.dto.LoginDTO;
import xiaozhi.modules.security.dto.SmsVerificationDTO;
import xiaozhi.modules.security.password.PasswordUtils;
import xiaozhi.modules.security.service.CaptchaService;
import xiaozhi.modules.security.service.SysUserTokenService;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.dto.AppUserDTO;
import xiaozhi.modules.sys.dto.PasswordDTO;
import xiaozhi.modules.sys.dto.RetrievePasswordDTO;
import xiaozhi.modules.sys.dto.SysUserDTO;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.service.SysDictDataService;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.sys.service.SysUserPlusService;
import xiaozhi.modules.sys.service.SysUserService;
import xiaozhi.modules.sys.vo.SysDictDataItem;

/**
 * 登录控制层
 */
@AllArgsConstructor
@RestController
@RequestMapping("/user")
@Tag(name = "登录管理")
public class LoginController {
    private final SysUserService sysUserService;
    private final SysUserTokenService sysUserTokenService;
    private final CaptchaService captchaService;
    private final SysParamsService sysParamsService;
    private final SysDictDataService sysDictDataService;
    private final SysUserPlusService sysUserPlusService;

    @GetMapping("/captcha")
    @Operation(summary = "验证码")
    public void captcha(HttpServletResponse response, String uuid) throws IOException {
        // uuid不能为空
        AssertUtils.isBlank(uuid, ErrorCode.IDENTIFIER_NOT_NULL);
        // 生成验证码
        captchaService.create(response, uuid);
    }

    @PostMapping("/smsVerification")
    @Operation(summary = "短信验证码")
    public Result<Void> smsVerification(@RequestBody SmsVerificationDTO dto) {
        // 验证图形验证码
        boolean validate = captchaService.validate(dto.getCaptchaId(), dto.getCaptcha(), true);
        if (!validate) {
            throw new RenException("图形验证码错误");
        }
        Boolean isMobileRegister = sysParamsService
                .getValueObject(Constant.SysMSMParam.SERVER_ENABLE_MOBILE_REGISTER.getValue(), Boolean.class);
        if (!isMobileRegister) {
            throw new RenException("没有开启手机注册，没法使用短信验证码功能");
        }
        // 发送短信验证码
        captchaService.sendSMSValidateCode(dto.getPhone());
        return new Result<>();
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<TokenDTO> login(@RequestBody LoginDTO login) {
        Assert.isTrue(StrUtil.isNotBlank(login.getUsername())||StrUtil.isNotBlank(login.getMobile()), "用户名或手机号不能为空");
        // 验证是否正确输入验证码
        boolean validate = captchaService.validate(login.getCaptchaId(), login.getCaptcha(), true);
        if (!validate) {
            throw new RenException("图形验证码错误，请重新获取");
        }

        SysUserEntity user = sysUserPlusService.getOne(Wrappers.lambdaQuery(SysUserEntity.class)
                .and(wrapper ->
                        wrapper.eq(StrUtil.isNotBlank(login.getMobile()),SysUserEntity::getMobile, login.getMobile())
                                .eq(StrUtil.isNotBlank(login.getUsername()),SysUserEntity::getUsername, login.getUsername())
                )
                .or().eq(StrUtil.isNotBlank(login.getUsername()),SysUserEntity::getMobile, login.getUsername())
        );

        // 判断用户是否存在
        if (user == null) {
            throw new RenException("请检测用户和密码是否输入错误");
        }
        // 判断密码是否正确，不一样则进入if
        if (!PasswordUtils.matches(login.getPassword(), user.getPassword())) {
            throw new RenException("请检测用户和密码是否输入错误");
        }

        Assert.isTrue(Arrays.asList("app","pc").contains(login.getLoginDevice()),"登入设备不支持");
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setRefreshToken(JwtUtil.createRefreshToken(user.getId(), login.getLoginDevice()));
        tokenDTO.setToken(JwtUtil.createToken(user.getId(),user.getUsername(), login.getLoginDevice()));
        tokenDTO.setClientHash(HttpContextUtils.getClientCode());
        tokenDTO.setExpire(3600);

        return new Result<TokenDTO>().ok(tokenDTO);
    }

    @PostMapping("/captchaLogin")
    @Operation(summary = "手机验证码登录")
    public Result<TokenDTO> captchaLogin(@RequestBody Map<String, String>  params) {

        String mobile = params.get("mobile");
        String loginDevice = params.get("loginDevice");
        String mobileCaptcha = params.get("mobileCaptcha");
        //String captchaId = params.get("captchaId");

        Assert.isTrue(StrUtil.isNotBlank(mobile)&&StrUtil.isNotBlank(mobileCaptcha), "手机号或验证码不能为空");
        // 验证是否正确输入验证码
        /*boolean validate = captchaService.validate(captchaId, captcha, true);
        if (!validate) {
            throw new RenException("图形验证码错误，请重新获取");
        }*/

        // 验证用户是否是手机号码
        boolean validPhone = Validator.isMobile(mobile);
        if (!validPhone) {
            throw new RenException("手机号码格式不正确，请重新输入");
        }
        // 验证短信验证码是否正常
        if (!captchaService.validateSMSValidateCode(mobile, mobileCaptcha, true)) {
            throw new RenException("手机验证码错误，请重新获取");
        }

        SysUserEntity user = sysUserPlusService.getOne(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getMobile, mobile));

        // 判断用户是否存在
        if (user == null) {
            throw new RenException("该手机号未注册");
        }

        Assert.isTrue(Arrays.asList("app","pc").contains(loginDevice),"登入设备不支持");
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setRefreshToken(JwtUtil.createRefreshToken(user.getId(), loginDevice));
        tokenDTO.setToken(JwtUtil.createToken(user.getId(),user.getUsername(), loginDevice));
        tokenDTO.setClientHash(HttpContextUtils.getClientCode());
        tokenDTO.setExpire(3600);

        return new Result<TokenDTO>().ok(tokenDTO);
    }

    @PostMapping("/refreshToken")
    @Operation(summary = "刷新token")
    @RateLimit(key_pre = "sys:refreshToken")
    public Result<Map<String, String>> refreshToken(@RequestHeader("refreshToken") String refreshToken) {
        System.out.println("请求刷新token接口refreshToken:" + refreshToken);
        Map<String, Object> map = JwtUtil.getValueFromRefreshToken(refreshToken);
        Long userId = (Long) map.get("userId");
        String deviceType = map.get("deviceType").toString();

        SysUserEntity user = sysUserPlusService.getOne(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getId, userId));
        Assert.notNull(user, "token异常，非法登入");

        return ResultUtils.success(JwtUtil.generateTokens(user.getId(),user.getUsername(),deviceType));
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Void> register(@RequestBody LoginDTO login) {
        if (!sysUserService.getAllowUserRegister()) {
            throw new RenException("当前不允许普通用户注册");
        }
        // 是否开启手机注册
        Boolean isMobileRegister = sysParamsService
                .getValueObject(Constant.SysMSMParam.SERVER_ENABLE_MOBILE_REGISTER.getValue(), Boolean.class);
        boolean validate;
        if (isMobileRegister) {
            // 验证用户是否是手机号码
            boolean validPhone = Validator.isMobile(login.getMobile());
            if (!validPhone) {
                throw new RenException("手机号码格式不正确，请重新输入");
            }
            if(!ObjectUtil.equals(login.getMobile(), login.getUsername())){
                boolean validUsername = Validator.isMobile(login.getUsername());
                if (validUsername) {
                    throw new RenException("用户名不能为手机号");
                }
            }
            // 验证短信验证码是否正常
            validate = captchaService.validateSMSValidateCode(login.getMobile(), login.getMobileCaptcha(), true);
            if (!validate) {
                throw new RenException("手机验证码错误，请重新获取");
            }
        } else {
            // 验证是否正确输入验证码
            validate = captchaService.validate(login.getCaptchaId(), login.getCaptcha(), true);
            if (!validate) {
                throw new RenException("图形验证码错误，请重新获取");
            }
        }

        // 按照用户名获取用户
        Assert.isFalse(sysUserPlusService.exists(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getUsername, login.getUsername())), "此用户名已经注册过");
        Assert.isFalse(sysUserPlusService.exists(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getMobile, login.getMobile())), "此手机号码已被注册");
        SysUserDTO userDTO = new SysUserDTO();
        userDTO.setUsername(login.getUsername());
        userDTO.setRealName(login.getRealName());
        userDTO.setMobile(login.getMobile());
        userDTO.setPassword(login.getPassword());
        sysUserService.save(userDTO);
        return new Result<>();
    }

    @GetMapping("/info")
    @Operation(summary = "用户信息获取")
    public Result<UserDetail> info() {
        UserDetail user = SecurityUser.getUser();
        Result<UserDetail> result = new Result<>();
        result.setData(user);
        return result;
    }

    @PutMapping("/change-password")
    @Operation(summary = "修改用户密码")
    public Result<?> changePassword(@RequestBody PasswordDTO passwordDTO) {
        // 判断非空
        ValidatorUtils.validateEntity(passwordDTO);
        Long userId = SecurityUser.getUserId();
        sysUserTokenService.changePassword(userId, passwordDTO);
        return new Result<>();
    }

    @PutMapping("/retrieve-password")
    @Operation(summary = "找回密码")
    public Result<?> retrievePassword(@RequestBody RetrievePasswordDTO dto) {
        // 是否开启手机注册
        Boolean isMobileRegister = sysParamsService
                .getValueObject(Constant.SysMSMParam.SERVER_ENABLE_MOBILE_REGISTER.getValue(), Boolean.class);
        if (!isMobileRegister) {
            throw new RenException("没有开启手机注册，没法使用找回密码功能");
        }
        // 判断非空
        ValidatorUtils.validateEntity(dto);
        // 验证用户是否是手机号码
        boolean validPhone = Validator.isMobile(dto.getPhone());
        if (!validPhone) {
            throw new RenException("输入的手机号码格式不正确");
        }

        // 按照用户名获取用户
        SysUserDTO userDTO = sysUserService.getByUsername(dto.getPhone());
        if (userDTO == null) {
            throw new RenException("输入的手机号码未注册");
        }
        // 验证短信验证码是否正常
        boolean validate = captchaService.validateSMSValidateCode(dto.getPhone(), dto.getCode(), true);
        // 判断是否通过验证
        if (!validate) {
            throw new RenException("输入的手机验证码错误");
        }

        sysUserService.changePasswordDirectly(userDTO.getId(), dto.getPassword());
        return new Result<>();
    }

    @GetMapping("/pub-config")
    @Operation(summary = "公共配置")
    public Result<Map<String, Object>> pubConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("enableMobileRegister", sysParamsService
                .getValueObject(Constant.SysMSMParam.SERVER_ENABLE_MOBILE_REGISTER.getValue(), Boolean.class));
        config.put("version", Constant.VERSION);
        config.put("year", "©" + Calendar.getInstance().get(Calendar.YEAR));
        config.put("allowUserRegister", sysUserService.getAllowUserRegister());
        List<SysDictDataItem> list = sysDictDataService.getDictDataByType(Constant.DictType.MOBILE_AREA.getValue());
        config.put("mobileAreaList", list);
        config.put("beianIcpNum", sysParamsService.getValue(Constant.SysBaseParam.BEIAN_ICP_NUM.getValue(), true));
        config.put("beianGaNum", sysParamsService.getValue(Constant.SysBaseParam.BEIAN_GA_NUM.getValue(), true));
        config.put("name", sysParamsService.getValue(Constant.SysBaseParam.SERVER_NAME.getValue(), true));
        config.put("menuSeasoningType", sysParamsService.getValue("system.menu_seasoning_type",true));
        config.put("fileUrl", sysParamsService.getValue("file.url",true));
        config.put("recipePicUrl", sysParamsService.getValue("recipe.pic.url",true));

        return new Result<Map<String, Object>>().ok(config);
    }

    @PutMapping("/updateLocal")
    @Operation(summary = "修改信息")
    public Result<Void> updateLocal(@RequestBody AppUserDTO dto) {
        UserDetail user = SecurityUser.getUser();
        LambdaUpdateWrapper<SysUserEntity> updateWrapper = Wrappers.lambdaUpdate(SysUserEntity.class);
        updateWrapper.eq(SysUserEntity::getId, user.getId());
        if(StrUtil.isNotBlank(dto.getUsername())&&!dto.getUsername().equals(user.getUsername())){
            Assert.isFalse(sysUserPlusService.exists(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getUsername, dto.getUsername()).ne(SysUserEntity::getId, user.getId())), "此用户名已经注册过");
            updateWrapper.set(SysUserEntity::getUsername, dto.getUsername());
        }
        if(StrUtil.isNotBlank(dto.getRealName())&&!dto.getRealName().equals(user.getRealName())){
            updateWrapper.set(SysUserEntity::getRealName, dto.getRealName());
        }
        if(StrUtil.isNotBlank(dto.getHeadUrl())&&!dto.getHeadUrl().equals(user.getHeadUrl())){
            updateWrapper.set(SysUserEntity::getHeadUrl, dto.getHeadUrl());
        }
        if(dto.getGender()!=null&&!dto.getGender().equals(user.getGender())){
            updateWrapper.set(SysUserEntity::getGender, dto.getGender());
        }
        sysUserPlusService.update(updateWrapper);

        return new Result<>();
    }
}