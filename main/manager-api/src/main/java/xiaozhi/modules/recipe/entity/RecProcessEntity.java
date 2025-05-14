package xiaozhi.modules.recipe.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.modules.recipe.dto.RecActionDTO;
import xiaozhi.modules.recipe.dto.RecProcessDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "recipe_process",autoResultMap = true)
@Schema(description = "步骤")
public class RecProcessEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "主键ID")
    private String infoId;

    @Schema(description = "时长")
    private String duration;

    @Schema(description = "温度")
    private String temperature;

    @Schema(description = "操作")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<RecProcessDTO.Action> actions;

    @Data
    @Schema(description = "操作")
    public static class Action{
        @Schema(description = "id")
        private String id;
        @Schema(description = "参数")
        private List<RecActionDTO.Param> args;

        @Schema(description = "操作方法")
        private String method;
        @Schema(description = "参数集")
        private Map<String,String> parameters;
    }

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

}
