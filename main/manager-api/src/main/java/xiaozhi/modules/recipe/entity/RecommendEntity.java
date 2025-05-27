package xiaozhi.modules.recipe.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@TableName("recipe_commend")
@Schema(description = "菜谱分类")
public class RecommendEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "菜品id")
    private String infoId;

    @Schema(description = "推荐人Id")
    private Long userId;

    @Schema(description = "审核人")
    private Long auditor;

    @Schema(description = "审核意见")
    private String auditIdea;

    @Schema(description = "审核状态，0:草稿，1推荐中，2：推荐成功,3:驳回")
    private Integer auditStatus;

    @Schema(description = "审核时间")
    private Date auditDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建者")
    @TableField(fill = FieldFill.INSERT)
    private Long creator;

    @Schema(description = "更新者")
    @TableField(fill = FieldFill.UPDATE)
    private Long updater;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.UPDATE)
    private Date updateDate;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

}
