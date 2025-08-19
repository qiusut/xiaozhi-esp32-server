package xiaozhi.modules.sys.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;
import xiaozhi.common.entity.BaseEntity;

/**
 * 系统用户
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class SysUserEntity extends BaseEntity {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 超级管理员 0：否 1：是
     */
    private Integer superAdmin;

    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "头像")
    private String headUrl;

    @Schema(description = "性别 0：男   1：女   2：未知")
    @Range(min = 0, max = 2, message = "性别不正确")
    private Integer gender;

    @Schema(description = "手机号")
    private String mobile;

    //@Schema(description = "用户类型 1:app用户,2:管理员用户")
    //private Integer usrType;
    /**
     * 状态 0：停用 1：正常
     */
    private Integer status;
    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "删除标识  0：正常   1：已删除")
    @TableLogic
    private Integer deleted;

}