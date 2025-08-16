package xiaozhi.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

import java.util.Date;

@Schema(description = "菜单管理")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_menu")
public class SysMenuEntity extends BaseEntity {


    @Schema(description = "上级ID")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long pid;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单URL")
    private String url;

    @Schema(description = "授权标识(多个用逗号分隔，如：sys:menu:list,sys:menu:save)")
    private String authority;

    @Schema(description = "类型   0：菜单   1：按钮   2：接口")
    private Integer type;

    @Schema(description = "打开方式   0：内部   1：外部")
    private Integer openStyle;

    @Schema(description = "菜单图标")
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String icon;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否隐藏 0 否 1 是")
    private Integer hidden;

    @Schema(description = "更新者")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "删除标识  0：正常   1：已删除")
    @TableLogic
    private Integer deleted;

}