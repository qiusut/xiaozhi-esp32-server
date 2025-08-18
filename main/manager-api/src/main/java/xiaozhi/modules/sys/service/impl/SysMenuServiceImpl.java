package xiaozhi.modules.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.common.user.UserDetail;
import xiaozhi.modules.sys.dao.SysMenuDao;
import xiaozhi.modules.sys.dto.SysMenuDTO;
import xiaozhi.modules.sys.entity.SysMenuEntity;
import xiaozhi.modules.sys.enums.SuperAdminEnum;
import xiaozhi.modules.sys.service.SysMenuService;
import xiaozhi.modules.sys.service.SysRoleMenuService;
import xiaozhi.modules.sys.utils.RequiresPermissionsUtil;
import xiaozhi.modules.sys.vo.SysMenuVO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理
 */
@Service
@AllArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuDao, SysMenuEntity> implements SysMenuService {
    private final SysRoleMenuService sysRoleMenuService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysMenuDTO dto) {
        SysMenuEntity entity = BeanUtil.copyProperties(dto, SysMenuEntity.class);

        // 保存菜单
        baseMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysMenuVO vo) {
        SysMenuEntity entity = BeanUtil.copyProperties(vo, SysMenuEntity.class);

        Assert.isFalse(entity.getId().equals(entity.getPid()),"上级菜单不能为自己");

        // 更新菜单
        updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 删除菜单
        removeById(id);

        // 删除角色菜单关系
        sysRoleMenuService.deleteByMenuId(id);
    }

    @Override
    public List<SysMenuVO> getMenuList(Integer type) {
        List<SysMenuVO> sysMenuVOList = new ArrayList<>();
        List<SysMenuEntity> menuList = this.list(Wrappers.lambdaQuery(SysMenuEntity.class)
                .eq(type!=null,SysMenuEntity::getType, type)
                .orderByAsc(SysMenuEntity::getSort)
        );

        //配置
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 自定义属性名 都有默认值的
        treeNodeConfig.setWeightKey("sort");
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setParentIdKey("pid");
        // 最大递归深度
        //treeNodeConfig.setDeep(3);

        if(CollUtil.isNotEmpty(menuList)){
            // 构建树形结构
            List<Tree<Long>> treeNodes = TreeUtil.build(menuList, null,
                    treeNodeConfig,
                    (treeNode, tree) -> {
                        tree.setId(treeNode.getId());
                        tree.setParentId(treeNode.getPid());
                        tree.setName(treeNode.getName());
                        tree.setWeight(treeNode.getSort());
                        // 设置扩展字段
                        tree.putExtra("url", treeNode.getUrl());
                        tree.putExtra("icon", treeNode.getIcon());
                        tree.putExtra("openStyle", treeNode.getOpenStyle());
                        tree.putExtra("type", treeNode.getType());
                        tree.putExtra("authority", treeNode.getAuthority());
                        tree.putExtra("hidden", treeNode.getHidden());
                        //tree.putExtra("createTime", treeNode.getCreateTime());
                        //tree.putExtra("parentName", treeNode.getParentName());
                    });
            sysMenuVOList = BeanUtil.copyToList(treeNodes, SysMenuVO.class);
        }

        return sysMenuVOList;
    }

    @Override
    public List<SysMenuVO> getUserMenuList(UserDetail user, Integer type) {
        List<SysMenuVO> sysMenuVOList = new ArrayList<>();
        List<SysMenuEntity> menuList;

        // 系统管理员，拥有最高权限
        if (user.getSuperAdmin().equals(SuperAdminEnum.YES.value())) {
            menuList = this.list(Wrappers.lambdaQuery(SysMenuEntity.class)
                    .eq(type!=null,SysMenuEntity::getType, type)
                    .orderByAsc(SysMenuEntity::getSort)
            );
        } else {
            menuList = baseMapper.getUserMenuList(user.getId(), type);
        }

        //配置
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 自定义属性名 都有默认值的
        treeNodeConfig.setWeightKey("sort");
        treeNodeConfig.setIdKey("id");
        treeNodeConfig.setParentIdKey("pid");
        // 最大递归深度
        //treeNodeConfig.setDeep(3);

        if(CollUtil.isNotEmpty(menuList)){
            // 2. 构建树形结构
            List<Tree<Long>> treeNodes = TreeUtil.build(menuList, null,
                    treeNodeConfig,
                    (treeNode, tree) -> {
                        tree.setId(treeNode.getId());
                        tree.setParentId(treeNode.getPid());
                        tree.setName(treeNode.getName());
                        tree.setWeight(treeNode.getSort());
                        // 设置扩展字段
                        tree.putExtra("url", treeNode.getUrl());
                        tree.putExtra("icon", treeNode.getIcon());
                        tree.putExtra("openStyle", treeNode.getOpenStyle());
                        tree.putExtra("type", treeNode.getType());
                        tree.putExtra("authority", treeNode.getAuthority());
                        tree.putExtra("hidden", treeNode.getHidden());
                        //tree.putExtra("createTime", treeNode.getCreateTime());
                        //tree.putExtra("parentName", treeNode.getParentName());
                    });
            sysMenuVOList = BeanUtil.copyToList(treeNodes, SysMenuVO.class);
        }

        return sysMenuVOList;
    }

    @Override
    public Long getSubMenuCount(Long pid) {
        return count(new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getPid, pid));
    }

    @Override
    public Set<String> getUserAuthority(UserDetail user) {
        // 系统管理员，拥有最高权限
        List<String> authorityList;
        if (user.getSuperAdmin().equals(SuperAdminEnum.YES.value())) {
            authorityList = baseMapper.getUserAuthorityList(user.getId());
            authorityList.addAll(RequiresPermissionsUtil.getRequiresPermissionsList());
        } else {
            authorityList = baseMapper.getUserAuthorityList(user.getId());
        }
        Set<String> permsSet = new HashSet<>();
        if(CollUtil.isNotEmpty(authorityList)){
            permsSet = authorityList.stream()
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .flatMap(authority -> Arrays.stream(authority.trim().split(",")))
                    .collect(Collectors.toSet());
        }

        // 用户权限列表
        /*Set<String> permsSet = new HashSet<>();
        for (String authority : authorityList) {
            if (StrUtil.isBlank(authority)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(authority.trim().split(",")));
        }*/

        return permsSet;
    }

}