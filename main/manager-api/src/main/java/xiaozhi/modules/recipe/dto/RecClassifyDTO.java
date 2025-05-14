package xiaozhi.modules.recipe.dto;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("rec_classify")
@Schema(description = "菜谱分类")
public class RecClassifyDTO {

    @Schema(description = "父级id")
    private String parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "层级")
    @NotBlank
    private Integer level;

    @Schema(description = "排序")
    @NotBlank
    private Integer sort;

}
