package xiaozhi.modules.bind.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@TableName(value = "device_share",autoResultMap = true)
@Schema(description = "设备共享表")
public class DeviceShareEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "设备类型，ai设备，tb设备")
    private String type;

    @Schema(description = "共享的设备id")
    private String deviceId;

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
