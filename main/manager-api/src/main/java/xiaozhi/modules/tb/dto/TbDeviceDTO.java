package xiaozhi.modules.tb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class TbDeviceDTO {

    @Schema(description = "tb设备id")
    @NotBlank(message = "tb设备id不能为空")
    private String tbDeviceId;

    @Schema(description = "名称,以这个名称匹配")
    private String name;

}
