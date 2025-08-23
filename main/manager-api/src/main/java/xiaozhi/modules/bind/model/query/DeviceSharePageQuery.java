package xiaozhi.modules.bind.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备信息分页查询
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "设备信息查询")
public class DeviceSharePageQuery {

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "类型(ai,tb)")
    private String type;

    @Schema(description = "当前页")
    private int page;

    @Schema(description = "每页结果数")
    private int limit;

}