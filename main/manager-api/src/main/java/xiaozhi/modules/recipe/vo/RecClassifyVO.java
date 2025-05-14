package xiaozhi.modules.recipe.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.utils.DateUtils;

import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "菜谱分类")
public class RecClassifyVO {

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
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date updateDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date createDate;

}
