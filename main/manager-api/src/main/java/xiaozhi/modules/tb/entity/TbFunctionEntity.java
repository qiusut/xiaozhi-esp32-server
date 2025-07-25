package xiaozhi.modules.tb.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_function",autoResultMap = true)
@Schema(description = "tb方法")
public class TbFunctionEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank
    @Schema(description = "tb类型")
    private String type;

    @Schema(description = "函数列表")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Function_call> funs;

    @Data
    public static class Function_call {

        @Schema(description = "函数描述")
        private String description;

        @Schema(description = "物联网调用方法")
        private String methodName;

        @Schema(description = "参数列表")
        @TableField(typeHandler = JacksonTypeHandler.class)
        private List<Function_call_params> params;

        @Data
        public static class Function_call_params {

            @Schema(description = "参数名称")
            private String name;

            @Schema(description = "参数类型")
            private String type;

            @Schema(description = "参数描述")
            private String description;

            @Schema(description = "是否必需,1：是，0否")
            private int is_required;

        }

    }

    @Schema(description = "状态，0禁用，1正常")
    private Integer status;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;

}
