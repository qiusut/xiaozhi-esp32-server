package xiaozhi.modules.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "菜谱")
public class RecInfoDTO {

    @Schema(description = "分类id")
    private String classifyId;

    @Schema(description = "菜谱名称")
    private String name;

    @Schema(description = "菜谱简介")
    private String intro;

    @Schema(description = "参考热量")
    private String referHot;

    @Schema(description = "菜谱详情")
    private String detail;

    @Schema(description = "所属用户")
    private Long userId;

    @Schema(description = "所属范围，0：公共，1：个人")
    private Integer scope;

    @Schema(description = "轮播图")
    private String carouselPic;

    @Schema(description = "状态，0禁用，1正常")
    private Integer status;

    @Schema(description = "操作步骤")
    private List<RecProcessDTO> processDTOS;

}
