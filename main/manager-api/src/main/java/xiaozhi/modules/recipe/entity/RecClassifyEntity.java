package xiaozhi.modules.recipe.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("recipe_classify")
@Schema(description = "菜谱分类")
public class RecClassifyEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "父级id")
    private String parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "层级")
    private Integer level;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

}
