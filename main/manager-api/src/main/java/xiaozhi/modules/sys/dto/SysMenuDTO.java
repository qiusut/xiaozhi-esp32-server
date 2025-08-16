package xiaozhi.modules.sys.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import xiaozhi.common.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 菜单管理
 */
@Data
@Schema(description = "菜单")
public class SysMenuDTO {

    @Schema(description = "上级ID")
    private Long pid;

    @Schema(description = "菜单名称")
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @Schema(description = "菜单URL")
    private String url;

    @Schema(description = "类型  0：菜单   1：按钮   2：接口")
    @Range(min = 0, max = 2, message = "类型不正确")
    private Integer type;

    @Schema(description = "打开方式   0：内部   1：外部")
    @Range(min = 0, max = 1, message = "打开方式不正确")
    private Integer openStyle;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "授权标识(多个用逗号分隔，如：sys:menu:list,sys:menu:save)")
    private String authority;

    @Schema(description = "是否隐藏 0 否 1 是")
    private Integer hidden;

    @Schema(description = "排序")
    @Min(value = 0, message = "排序值不能小于0")
    private Integer sort;

}