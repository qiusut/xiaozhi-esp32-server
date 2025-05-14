package xiaozhi.modules.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "步骤")
public class RecProcessDTO {

    @Schema(description = "时长")
    private String duration;

    @Schema(description = "温度")
    private String temperature;

    @Schema(description = "操作")
    private List<Action> actions;

    @Data
    @Schema(description = "操作")
    public static class Action{
        @Schema(description = "id")
        private String id;
        @Schema(description = "参数")
        private List<RecActionDTO.Param> args;
        @NotBlank
        @Schema(description = "操作方法")
        private String method;
        @Schema(description = "参数集（key:value形式，key为参数名，value为填的值）")
        private Map<String,String> parameters;
    }

    @Schema(description = "排序")
    @NotBlank
    private Integer sort;

}
