package xiaozhi.modules.sys.service;




import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.common.user.UserDetail;
import xiaozhi.modules.sys.dto.SysMenuDTO;
import xiaozhi.modules.sys.entity.SysMenuEntity;
import xiaozhi.modules.sys.vo.SysMenuVO;

import java.util.List;
import java.util.Set;


/**
 * 菜单管理
 */
public interface SysMenuService extends IService<SysMenuEntity> {

	void save(SysMenuDTO dto);

	void update(SysMenuVO vo);

	void delete(Long id);

	/**
	 * 菜单列表
	 *
	 * @param type 菜单类型
	 */
	List<SysMenuVO> getMenuList(Integer type);

	/**
	 * 用户菜单列表
	 *
	 * @param user  用户
	 * @param type 菜单类型
	 */
	List<SysMenuVO> getUserMenuList(UserDetail user, Integer type);

	/**
	 * 获取子菜单的数量
	 * @param pid  父菜单ID
	 */
	Long getSubMenuCount(Long pid);

	/**
	 * 获取用户权限列表
	 */
	Set<String> getUserAuthority(UserDetail user);
}
