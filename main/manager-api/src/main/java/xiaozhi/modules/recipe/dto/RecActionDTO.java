package xiaozhi.modules.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "操作")
public class RecActionDTO {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "操作方法")
    private String method;

    @Schema(description = "参数")
    private List<Param> args;

    @Data
    @EqualsAndHashCode(callSuper = false)
    @Schema(description = "参数")
    public static class Param{
        @NotBlank
        @Schema(description = "参数标签")
        private String label;
        @NotBlank
        @Schema(description = "参数名称")
        private String name;
        @NotBlank
        @Schema(description = "参数单位")
        private String unit;
        @NotBlank
        @Schema(description = "是否必须,1:是,0:否")
        private Integer isRequired;

    }

}
