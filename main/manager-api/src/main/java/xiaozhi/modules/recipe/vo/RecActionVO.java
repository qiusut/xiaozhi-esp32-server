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
@Schema(description = "操作")
public class RecActionVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "操作方法")
    private String method;

    @Schema(description = "参数")
    private List<Param> args;

    @Data
    public static class Param{
        @Schema(description = "参数标签")
        private String label;
        @Schema(description = "参数名称")
        private String name;
        @Schema(description = "参数单位")
        private String unit;
        @Schema(description = "是否必须,1:是,0:否")
        private Integer isRequired;

    }


    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date updateDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date createDate;
}
