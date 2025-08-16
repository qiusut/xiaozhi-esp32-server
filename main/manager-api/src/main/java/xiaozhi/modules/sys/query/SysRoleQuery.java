package xiaozhi.modules.sys.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色管理
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "角色查询")
public class SysRoleQuery {
    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "当前页")
    private int page;

    @Schema(description = "每页结果数")
    private int limit;

}
