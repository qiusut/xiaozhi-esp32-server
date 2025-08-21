package xiaozhi.modules.sys.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.page.PageData;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.Result;
import xiaozhi.common.utils.ResultUtils;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.entity.SysRoleEntity;
import xiaozhi.modules.sys.query.SysRoleQuery;
import xiaozhi.modules.sys.service.*;
import xiaozhi.modules.sys.vo.SysMenuVO;
import xiaozhi.modules.sys.vo.SysRoleVO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理
 */
@RestController
@RequestMapping("admin/role")
@Tag(name = "角色管理")
@AllArgsConstructor
public class SysRoleController {
    private final SysRoleService sysRoleService;
    private final SysRoleMenuService sysRoleMenuService;
    private final SysMenuService sysMenuService;
    private final SysUserRoleService sysUserRoleService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @RequiresPermissions("admin:role:list")
    public Result<PageData<SysRoleVO>> page(@ParameterObject @Valid SysRoleQuery query) {
        Page<SysRoleEntity> page = sysRoleService.page(query);
        return ResultUtils.success(PageData.ok(BeanUtil.copyToList(page.getRecords(), SysRoleVO.class),page.getTotal()));
    }

    @GetMapping("list")
    @Operation(summary = "列表")
    @RequiresPermissions("admin:role:list")
    public Result<List<SysRoleVO>> list() {
        List<SysRoleVO> list = sysRoleService.getList(new SysRoleQuery());

        return ResultUtils.success(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    //@RequiresPermissions("sys:role:info")
    public Result<SysRoleVO> get(@PathVariable("id") Long id) {
        SysRoleEntity entity = sysRoleService.getById(id);

        // 转换对象
        SysRoleVO role = BeanUtil.copyProperties(entity, SysRoleVO.class);

        // 查询角色对应的菜单
        List<Long> menuIdList = sysRoleMenuService.getMenuIdList(id);
        role.setMenuIdList(menuIdList);

        return ResultUtils.success(role);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("新增角色")
    @RequiresPermissions("sys:role:save")
    public Result<String> save(@RequestBody @Valid SysRoleVO vo) {
        sysRoleService.save(vo);

        return ResultUtils.success(null);
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改角色")
    @RequiresPermissions("sys:role:update")
    public Result<String> update(@RequestBody @Valid SysRoleVO vo) {
        sysRoleService.update(vo);

        return ResultUtils.success(null);
    }

    /*@PutMapping("data-scope")
    @Operation(summary = "数据权限")
    @LogOperation("数据权限")
    @RequiresPermissions("sys:role:update")
    public Result<String> dataScope(@RequestBody @Valid SysRoleDataScopeVO vo) {
        sysRoleService.dataScope(vo);

        return ResultUtils.success(null);
    }*/

    @PostMapping("/delete")
    @Operation(summary = "删除")
    @LogOperation("删除角色")
    @RequiresPermissions("sys:role:delete")
    public Result<String> delete(@RequestBody Long[] idList) {
        if(ObjectUtil.isNotEmpty(idList)){
            sysRoleService.delete(CollUtil.toList(idList));
        }

        return ResultUtils.success(null);
    }

    @GetMapping("menu")
    @Operation(summary = "角色菜单")
    //@RequiresPermissions("sys:role:menu")
    public Result<List<SysMenuVO>> menu() {
        UserDetail user = SecurityUser.getUser();
        List<SysMenuVO> list = sysMenuService.getUserMenuList(user, null);

        return ResultUtils.success(list);
    }

    /*@GetMapping("user/page")
    @Operation(summary = "角色用户-分页")
    @RequiresPermissions("sys:role:update")
    public Result<PageResult<SysUserVO>> userPage(@Valid SysRoleUserQuery query) {
        PageResult<SysUserVO> page = sysUserService.roleUserPage(query);

        return ResultUtils.success(page);
    }*/

    @DeleteMapping("user/{roleId}")
    @Operation(summary = "删除角色用户")
    @LogOperation("删除角色用户")
    @RequiresPermissions("sys:role:update")
    public Result<String> userDelete(@PathVariable("roleId") Long roleId, @RequestBody List<Long> userIdList) {
        sysUserRoleService.deleteByUserIdList(roleId, userIdList);

        return ResultUtils.success(null);
    }

    @PostMapping("user/{roleId}")
    @Operation(summary = "分配角色给用户列表")
    @LogOperation("分配角色给用户列表")
    @RequiresPermissions("sys:role:update")
    public Result<String> userSave(@PathVariable("roleId") Long roleId, @RequestBody List<Long> userIdList) {
        Assert.isTrue(CollUtil.isNotEmpty(userIdList), "UserId is empty!");
        //查询数据库该角色对应的用户列表
        List<Long> existsUserIdList = sysUserRoleService.getExistsUserIdList(roleId);

        //取出需要新增的用户列表
        List<Long> addUserIdList = userIdList.stream()
            .filter(userId -> !existsUserIdList.contains(userId))
            .collect(Collectors.toList());
        Assert.isTrue(CollUtil.isNotEmpty(addUserIdList), "UserId existed!");
        sysUserRoleService.saveUserList(roleId, addUserIdList);

        return ResultUtils.success(null);
    }

    @PostMapping("nameList")
    @Operation(summary = "名称列表")
    public Result<List<String>> nameList(@RequestBody List<Long> idList) {
        List<String> list = sysRoleService.getNameList(idList);

        return ResultUtils.success(list);
    }
}