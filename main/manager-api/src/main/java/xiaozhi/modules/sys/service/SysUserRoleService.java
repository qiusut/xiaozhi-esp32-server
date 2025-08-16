package xiaozhi.modules.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.sys.entity.SysUserRoleEntity;

import java.util.List;

/**
 * 用户角色关系
 */
public interface SysUserRoleService extends IService<SysUserRoleEntity> {

    /**
     * 保存或修改
     * @param userId      用户ID
     * @param roleIdList  角色ID列表
     */
    void saveOrUpdate(Long userId, List<Long> roleIdList);

    /**
     * 分配角色给用户列表
     * @param roleId      角色ID
     * @param userIdList  用户ID列表
     */
    void saveUserList(Long roleId, List<Long> userIdList);

    /**
     * 根据角色id列表，删除用户角色关系
     * @param roleIdList 角色id
     */
    void deleteByRoleIdList(List<Long> roleIdList);

    /**
     * 根据用户id列表，删除用户角色关系
     * @param userIdList 用户id列表
     */
    void deleteByUserIdList(List<Long> userIdList);

    /**
     * 根据角色id、用户id列表，删除用户角色关系
     * @param roleId 角色id
     * @param userIdList 用户id列表
     */
    void deleteByUserIdList(Long roleId, List<Long> userIdList);

    /**
     * 角色ID列表
     * @param userId  用户ID
     */
    List<Long> getRoleIdList(Long userId);

    /**
     * 根据角色ID查询对应的用户ID列表
     * @param roleId  角色ID
     */
    List<Long> getExistsUserIdList(Long roleId);
}