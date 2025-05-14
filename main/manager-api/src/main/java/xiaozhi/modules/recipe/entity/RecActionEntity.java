package xiaozhi.modules.recipe.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "recipe_action",autoResultMap = true)
@Schema(description = "操作")
public class RecActionEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "操作方法")
    private String method;

    @Schema(description = "参数")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Param> args;

    @Data
    public static class Param{
        // 必须有无参构造函数（Fastjson/Jackson/Gson 通用要求）
        //public Param() {}

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
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

}
