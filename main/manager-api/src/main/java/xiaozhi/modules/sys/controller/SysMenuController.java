package xiaozhi.modules.sys.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.dto.SysMenuDTO;
import xiaozhi.modules.sys.entity.SysMenuEntity;
import xiaozhi.modules.sys.enums.MenuTypeEnum;
import xiaozhi.modules.sys.service.SysMenuService;
import xiaozhi.modules.sys.vo.SysMenuVO;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("admin/menu")
@Tag(name = "菜单管理")
@AllArgsConstructor
public class SysMenuController {
    private final SysMenuService sysMenuService;

    @GetMapping("nav")
    @Operation(summary = "菜单导航")
    //@RequiresPermissions("admin:menu:nav")
    public Result<List<SysMenuVO>> nav() {
        UserDetail user = SecurityUser.getUser();
        List<SysMenuVO> list = sysMenuService.getUserMenuList(user, MenuTypeEnum.MENU.getValue());

        return Result.okResult(list);
    }

    @GetMapping("authority")
    @Operation(summary = "用户权限标识")
    public Result<Set<String>> authority() {
        UserDetail user = SecurityUser.getUser();
        Set<String> set = sysMenuService.getUserAuthority(user);

        return Result.okResult(set);
    }

    @GetMapping("list")
    @Operation(summary = "菜单列表")
    @Parameter(name = "type", description = "菜单类型 0：菜单 1：按钮  2：接口  null：全部")
    //@RequiresPermissions("sys:menu:list")
    public Result<List<SysMenuVO>> list(Integer type) {
        List<SysMenuVO> list = sysMenuService.getMenuList(type);

        return Result.okResult(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    //@RequiresPermissions("sys:menu:info")
    public Result<SysMenuVO> get(@PathVariable("id") Long id) {
        SysMenuEntity entity = sysMenuService.getById(id);
        SysMenuVO vo = BeanUtil.copyProperties(entity, SysMenuVO.class);

        // 获取上级菜单名称
        if (entity.getPid() != null) {
            SysMenuEntity parentEntity = sysMenuService.getById(entity.getPid());
            vo.setParentName(parentEntity.getName());
        }

        return Result.okResult(vo);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("菜单保存")
    @RequiresPermissions("admin:menu:save")
    public Result<String> save(@RequestBody @Valid SysMenuDTO dto) {
        sysMenuService.save(dto);
        return Result.okResult(null);
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("菜单修改")
    @RequiresPermissions("admin:menu:update")
    public Result<Void> update(@RequestBody @Valid SysMenuVO vo) {
        sysMenuService.update(vo);
        return Result.okResult(null);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除")
    @LogOperation("菜单删除")
    @RequiresPermissions("admin:menu:delete")
    public Result<String> delete(@PathVariable("id") Long id) {
        // 判断是否有子菜单或按钮
        Long count = sysMenuService.getSubMenuCount(id);
        Assert.isFalse(count > 0, "请先删除子菜单");

        sysMenuService.delete(id);

        return Result.okResult(null);
    }
}