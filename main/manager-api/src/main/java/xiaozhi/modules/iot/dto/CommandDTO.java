package xiaozhi.modules.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class CommandDTO {

    @Schema(description = "设备mac地址")
    @NotBlank(message = "设备mac地址不能为空")
    private String device_mac;

    @Schema(description = "模块名称")
    @NotBlank(message = "模块名称不能为空")
    private String name;

    @Schema(description = "方法")
    @NotBlank(message = "方法不能为空")
    private String method;

    @Schema(description = "参数,key为参数名,value为参数值")
    private Map<String, Object> parameters;

}
