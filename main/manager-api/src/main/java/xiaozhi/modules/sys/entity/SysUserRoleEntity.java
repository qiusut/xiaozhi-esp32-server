package xiaozhi.modules.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper=false)
@Schema(description = "用户角色关系")
@TableName("sys_user_role")
public class SysUserRoleEntity extends BaseEntity {

    @Schema(description = "角色ID")
	private Long roleId;

    @Schema(description = "用户ID")
	private Long userId;

}
