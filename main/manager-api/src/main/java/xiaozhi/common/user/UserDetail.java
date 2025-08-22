package xiaozhi.common.user;

import java.io.Serializable;
import java.util.Set;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import xiaozhi.modules.sys.service.SysParamsService;

/**
 * 登录用户信息
 * Copyright (c) 人人开源 All rights reserved.
 * Website: https://www.renren.io
 */
@Data
public class UserDetail implements Serializable {
    private Long id;
    private String username;
    private Integer superAdmin;
    private String token;
    private Integer status;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "头像")
    private String headUrl;

    public String getHeadUrl(){
        if(StrUtil.isNotBlank(headUrl)){
            if(!headUrl.startsWith("http")){
                SysParamsService sysParamsService = SpringUtil.getBean(SysParamsService.class);
                return sysParamsService.getValue("file.url", true)+headUrl;
            }
        }
        return headUrl;
    }

    @Schema(description = "性别 0：男   1：女   2：未知")
    private Integer gender;

    @Schema(description = "手机号")
    private String mobile;

    private Set<String> permsSet;
}