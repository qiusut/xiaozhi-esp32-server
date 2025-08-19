package xiaozhi.modules.recipe.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.common.validator.AssertUtils;
import xiaozhi.modules.recipe.dto.RecActionDTO;
import xiaozhi.modules.recipe.entity.RecActionEntity;
import xiaozhi.modules.recipe.service.RecActionService;
import xiaozhi.modules.recipe.vo.RecActionVO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 操作管理
 */
@RestController
@RequestMapping("/recipe/action")
@Tag(name = "操作管理")
@AllArgsConstructor
public class RecActionController {
    private final RecActionService recActionService;

    @GetMapping("/page")
    @Operation(summary = "操作分页查询")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true)
    })
    //@RequiresPermissions("recipe:action:page")
    public Result<PageData<RecActionVO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {

        Page<RecActionEntity> page = recActionService.getPage(params);
        return new Result<PageData<RecActionVO>>().ok(
                new PageData<RecActionVO>(
                        BeanUtil.copyToList(page.getRecords(), RecActionVO.class), page.getTotal()
                )
        );
    }

    @GetMapping("/list")
    @Operation(summary = "获取操作列表")
    //@RequiresPermissions("sys:role:normal")
    public Result<List<RecActionVO>> list() {
        List<RecActionEntity> list = recActionService.list();
        return new Result<List<RecActionVO>>().ok(BeanUtil.copyToList(list, RecActionVO.class));
    }

    @GetMapping("{id}")
    @Operation(summary = "操作信息")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<RecActionVO> get(@PathVariable("id") String id) {
        RecActionEntity recActionEntity = recActionService.getById(id);
        return new Result<RecActionVO>().ok(BeanUtil.copyProperties(recActionEntity, RecActionVO.class));
    }

    @PostMapping
    @Operation(summary = "操作保存")
    @LogOperation("操作保存")
    @RequiresPermissions("recipe:action:save")
    public Result<Void> save(@RequestBody RecActionDTO dto) {
        RecActionEntity entity = BeanUtil.copyProperties(dto, RecActionEntity.class);
        //entity.setCreateDate(new Date());
        //entity.setUpdateDate(new Date());
        recActionService.save(entity);
        return new Result<Void>();
    }

    @PutMapping
    @Operation(summary = "操作修改")
    @LogOperation("操作修改")
    @RequiresPermissions("recipe:action:update")
    public Result<Void> update(@RequestBody RecActionVO vo) {
        RecActionEntity entity = BeanUtil.copyProperties(vo, RecActionEntity.class);
        recActionService.updateById(entity);
        return new Result<Void>();
    }

    @PostMapping("/delete")
    @Operation(summary = "操作删除")
    @LogOperation("操作删除")
    @RequiresPermissions("recipe:action:delete")
    public Result<Void> delete(@RequestBody String[] ids) {
        // 效验数据
        AssertUtils.isArrayEmpty(ids, "id");
        recActionService.removeBatchByIds(List.of(ids));
        return new Result<Void>();
    }
}
