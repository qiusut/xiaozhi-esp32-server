package xiaozhi.modules.sys.service;



import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.sys.entity.SysRoleEntity;
import xiaozhi.modules.sys.query.SysRoleQuery;
import xiaozhi.modules.sys.vo.SysRoleVO;

import java.util.List;

/**
 * 角色
 */
public interface SysRoleService extends IService<SysRoleEntity> {

    Page<SysRoleEntity> page(SysRoleQuery query);

    List<SysRoleVO> getList(SysRoleQuery query);

    void save(SysRoleVO vo);

    void update(SysRoleVO vo);

    void delete(List<Long> idList);

    /**
     * 获取角色名称列表
     *
     * @param idList 角色ID列表
     * @return 角色名称列表
     */
    List<String> getNameList(List<Long> idList);
}
