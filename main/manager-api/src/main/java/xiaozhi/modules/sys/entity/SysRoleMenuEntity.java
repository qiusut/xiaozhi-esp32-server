package xiaozhi.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

@Schema(description = "角色菜单关系")
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_role_menu")
public class SysRoleMenuEntity extends BaseEntity {

    @Schema(description = "角色ID")
	private Long roleId;

    @Schema(description = "菜单ID")
	private Long menuId;

}
