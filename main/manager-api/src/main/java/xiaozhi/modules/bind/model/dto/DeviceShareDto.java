package xiaozhi.modules.bind.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "设备共享表")
public class DeviceShareDto {

    @NotBlank
    @Schema(description = "关联用户手机号")
    private String mobile;

    @Schema(description = "设备类型，ai设备，tb设备")
    private String type;

    @Schema(description = "共享的设备id")
    @NotBlank
    private String deviceId;

}
