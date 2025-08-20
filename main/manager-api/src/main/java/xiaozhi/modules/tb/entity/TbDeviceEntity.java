package xiaozhi.modules.tb.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_device",autoResultMap = true)
@Schema(description = "tb设备绑定表")
public class TbDeviceEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "名称,以这个名称匹配")
    private String name;

    @Schema(description = "tb类型")
    private String type;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "绑定的tb设备id")
    private String tbDeviceId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态，0禁用，1正常")
    private Integer status;

    @Schema(description = "创建者")
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @Schema(description = "更新者")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

    @Schema(description = "删除标记")
    @TableLogic
    private Integer deleted;

}
