package xiaozhi.modules.recipe.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.utils.DateUtils;
import xiaozhi.modules.recipe.dto.RecActionDTO;
import xiaozhi.modules.recipe.dto.RecProcessDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "步骤")
public class RecProcessVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "主键ID")
    private String infoId;

    @Schema(description = "时长")
    private String duration;

    @Schema(description = "温度")
    private String temperature;

    @Schema(description = "操作")
    private List<RecProcessVO.Action> actions;

    @Data
    @Schema(description = "操作")
    public static class Action{
        @Schema(description = "id")
        private String id;
        @Schema(description = "名称")
        private String name;
        @Schema(description = "参数")
        private List<RecActionVO.Param> args;

        @Schema(description = "操作方法")
        private String method;
        @Schema(description = "参数集")
        private Map<String,String> parameters;
    }

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date updateDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date createDate;

}
