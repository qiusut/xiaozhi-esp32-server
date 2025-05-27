package xiaozhi.modules.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "菜谱推荐")
public class RecommendDTO {

    @Schema(description = "菜品id")
    private String infoId;

    @Schema(description = "备注")
    private String remark;

}
