package xiaozhi.modules.recipe.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "菜谱")
public class RecInfoServerVO {

    @Schema(description = "菜谱名称")
    private String name;

    @Schema(description = "菜谱简介")
    private String intro;

    @Schema(description = "参考热量")
    private String referHot;

    @Schema(description = "菜谱详情")
    private String detail;

    @Schema(description = "步骤")
    private List<ProcessServerVO> processVOS;

    @Data
    public static class ProcessServerVO{
        @Schema(description = "时长")
        private String duration;

        @Schema(description = "温度")
        private String temperature;

        @Schema(description = "操作")
        private List<Action> actions;

        @Data
        @Schema(description = "操作")
        public static class Action{
            @Schema(description = "操作方法")
            private String method;
            @Schema(description = "参数集")
            private Map<String,String> parameters;
        }

        @Schema(description = "排序")
        private Integer sort;

    }


}
