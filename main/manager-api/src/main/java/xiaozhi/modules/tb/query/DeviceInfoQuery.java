package xiaozhi.modules.tb.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备信息查询
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "设备信息查询")
public class DeviceInfoQuery{

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "设备活动")
    private Boolean active;

    @Schema(description = "设备名称")
    private String textSearch;

}