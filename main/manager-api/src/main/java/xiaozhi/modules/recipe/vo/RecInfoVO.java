package xiaozhi.modules.recipe.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.utils.DateUtils;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "菜谱")
public class RecInfoVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "分类id")
    private String classifyId;

    @Schema(description = "分类名称")
    private String classifyName;

    @Schema(description = "菜谱名称")
    private String name;

    @Schema(description = "菜谱简介")
    private String intro;

    @Schema(description = "参考热量")
    private String referHot;

    @Schema(description = "菜谱详情")
    private String detail;

    @Schema(description = "状态，0禁用，1正常")
    private Integer status;

    @Schema(description = "步骤")
    private List<RecProcessVO> processVOS;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date updateDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date createDate;

}
