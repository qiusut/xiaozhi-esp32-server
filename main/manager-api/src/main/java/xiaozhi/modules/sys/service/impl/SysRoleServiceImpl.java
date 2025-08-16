package xiaozhi.modules.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.modules.security.service.SysUserTokenService;
import xiaozhi.modules.sys.dao.SysRoleDao;
import xiaozhi.modules.sys.entity.SysRoleEntity;
import xiaozhi.modules.sys.query.SysRoleQuery;
import xiaozhi.modules.sys.service.SysRoleMenuService;
import xiaozhi.modules.sys.service.SysRoleService;
import xiaozhi.modules.sys.service.SysUserRoleService;
import xiaozhi.modules.sys.vo.SysRoleVO;

import java.util.List;

/**
 * 角色
 */
@Service
@AllArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleDao, SysRoleEntity> implements SysRoleService {
    private final SysRoleMenuService sysRoleMenuService;
    private final SysUserRoleService sysUserRoleService;
    private final SysUserTokenService sysUserTokenService;

    @Override
    public Page<SysRoleEntity> page(SysRoleQuery query) {
        Page<SysRoleEntity> page = Page.of(query.getPage(), query.getLimit());
        baseMapper.selectPage(page, getWrapper(query));

        return page;
    }

    @Override
    public List<SysRoleVO> getList(SysRoleQuery query) {
        List<SysRoleEntity> entityList = baseMapper.selectList(getWrapper(query));

        return BeanUtil.copyToList(entityList, SysRoleVO.class);
    }

    private Wrapper<SysRoleEntity> getWrapper(SysRoleQuery query) {
        LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(query.getName()), SysRoleEntity::getName, query.getName());


        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysRoleVO vo) {
        SysRoleEntity entity = BeanUtil.copyProperties(vo, SysRoleEntity.class);

        // 保存角色
        //entity.setDataScope(DataScopeEnum.SELF.getValue());
        baseMapper.insert(entity);

        // 保存角色菜单关系
        sysRoleMenuService.saveOrUpdate(entity.getId(), vo.getMenuIdList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysRoleVO vo) {
        SysRoleEntity entity = BeanUtil.copyProperties(vo, SysRoleEntity.class);

        // 更新角色
        updateById(entity);

        // 更新角色菜单关系
        sysRoleMenuService.saveOrUpdate(entity.getId(), vo.getMenuIdList());

        // 更新角色对应用户的缓存权限
        //sysUserTokenService.updateCacheAuthByRoleId(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> idList) {
        // 删除角色
        removeByIds(idList);

        // 删除用户角色关系
        sysUserRoleService.deleteByRoleIdList(idList);

        // 删除角色菜单关系
        sysRoleMenuService.deleteByRoleIdList(idList);

        // 更新角色对应用户的缓存权限
        //idList.forEach(sysUserTokenService::updateCacheAuthByRoleId);
    }

    @Override
    public List<String> getNameList(List<Long> idList) {
        if (idList.isEmpty()) {
            return null;
        }

        return baseMapper.selectBatchIds(idList).stream().map(SysRoleEntity::getName).toList();
    }

}