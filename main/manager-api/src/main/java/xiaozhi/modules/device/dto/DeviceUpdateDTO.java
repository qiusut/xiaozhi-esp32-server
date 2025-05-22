package xiaozhi.modules.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 设备绑定的DTO
 */
@Data
@Schema(description = "设备修改信息")
public class DeviceUpdateDTO {

    @Schema(description = "id")
    private String id;

    @Schema(description = "设备别名")
    private String alias;

    @Schema(description = "设备备注")
    private String remark;

}