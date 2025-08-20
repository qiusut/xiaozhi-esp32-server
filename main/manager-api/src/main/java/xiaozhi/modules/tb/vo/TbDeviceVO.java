package xiaozhi.modules.tb.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.utils.DateUtils;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "tb设备")
public class TbDeviceVO {

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

    @Schema(description = "实体类型")
    private String entityType;

    @Schema(description = "是否在线")
    private Boolean active;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态，0禁用，1正常")
    private Integer status;

    @Schema(description = "创建者")
    private Long creator;

    @Schema(description = "更新者")
    private Long updater;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date updateDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date createDate;

}
