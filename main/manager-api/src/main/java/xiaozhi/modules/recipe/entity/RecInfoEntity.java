package xiaozhi.modules.recipe.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("recipe_info")
@Schema(description = "菜谱")
public class RecInfoEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "分类id")
    private String classifyId;

    @Schema(description = "菜谱名称")
    private String name;

    @Schema(description = "菜谱简介")
    private String intro;

    @Schema(description = "参考热量")
    private String referHot;

    @Schema(description = "所属用户")
    private Long userId;

    @Schema(description = "所属范围，0：公共，1：个人")
    private Integer scope;

    @Schema(description = "菜谱详情")
    private String detail;

    @Schema(description = "状态，0禁用，1正常")
    private Integer status;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

}
