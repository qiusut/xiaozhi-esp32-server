package xiaozhi.modules.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 设备更新DTO
 */
@Data
@Schema(description = "设备修改信息")
public class DeviceUpdateDTO implements Serializable {

    @Schema(description = "id")
    private String id;

    /**
    * 自动更新状态
    */
    @Max(1)
    @Min(0)
    private Integer autoUpdate;

    /**
    * 设备别名
    */
    @Size(max = 64)
    private String alias;

    @Schema(description = "设备备注")
    private String remark;

    private static final long serialVersionUID = 1L;
}
