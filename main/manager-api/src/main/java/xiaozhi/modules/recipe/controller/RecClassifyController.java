package xiaozhi.modules.recipe.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.utils.Result;
import xiaozhi.common.validator.AssertUtils;
import xiaozhi.modules.recipe.dto.RecClassifyDTO;
import xiaozhi.modules.recipe.entity.RecClassifyEntity;
import xiaozhi.modules.recipe.service.RecClassifyService;
import xiaozhi.modules.recipe.vo.RecClassifyVO;

import java.util.List;

/**
 * 菜单分类管理
 */
@RestController
@RequestMapping("/recipe/classify")
@Tag(name = "菜谱分类管理")
@AllArgsConstructor
public class RecClassifyController {
    private final RecClassifyService recClassifyService;

    @Operation(operationId = "获取Tree")
    @PostMapping("getTree")
    public Result<List<Tree<String>>> getTree() {

        List<Tree<String>> treeList = recClassifyService.getTree();

        return new Result<List<Tree<String>>>().ok(treeList);
    }

    @GetMapping("{id}")
    @Operation(operationId = "分类信息")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<RecClassifyDTO> get(@PathVariable("id") String id) {
        RecClassifyEntity recClassifyEntity = recClassifyService.getById(id);
        RecClassifyDTO data = BeanUtil.copyProperties(recClassifyEntity, RecClassifyDTO.class);
        return new Result<RecClassifyDTO>().ok(data);
    }

    @PostMapping
    @Operation(operationId = "分类保存")
    @LogOperation("分类保存")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> save(@RequestBody RecClassifyDTO dto) {
        RecClassifyEntity entity = BeanUtil.copyProperties(dto, RecClassifyEntity.class);
        if (StrUtil.isBlank(entity.getParentId())) {
            entity.setParentId("-1");
        }else{
            if(!StrUtil.equals(entity.getParentId(),"-1")){
                Assert.isFalse(recClassifyService.getById(entity.getParentId())==null, "父级id不存在");
            }
        }
        recClassifyService.save(entity);
        return new Result<Void>();
    }

    @PutMapping
    @Operation(operationId = "分类修改")
    @LogOperation("分类修改")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> update(@RequestBody RecClassifyVO vo) {
        RecClassifyEntity entity_old = recClassifyService.getById(vo.getId());
        if(!StrUtil.equals(entity_old.getParentId(),vo.getParentId())){
            Assert.isFalse(recClassifyService.getById(vo.getParentId())==null, "父级id不存在");
        }
        RecClassifyEntity entity = BeanUtil.copyProperties(vo, RecClassifyEntity.class);
        recClassifyService.updateById(entity);

        return new Result<Void>();
    }

    @PostMapping("delete")
    @Operation(operationId = "分类删除")
    @LogOperation("分类删除")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> delete(@RequestBody String[] ids) {
        // 效验数据
        AssertUtils.isArrayEmpty(ids, "id");
        recClassifyService.delete(List.of(ids));
        return new Result<Void>();
    }
}
