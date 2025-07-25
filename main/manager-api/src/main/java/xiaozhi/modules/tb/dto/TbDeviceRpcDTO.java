package xiaozhi.modules.tb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TbDeviceRpcDTO {

    @Schema(description = "智能体ID")
    @NotBlank(message = "设备id不能为空")
    private String agentId;

    @Schema(description = "设备id")
    @NotBlank(message = "设备id不能为空")
    private String deviceId;

    @NotBlank
    @Schema(description = "方法名称")
    private String method;

    @Schema(description = "参数")
    private Map params;




}
