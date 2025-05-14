package xiaozhi.modules.recipe.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.annotation.LogOperation;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.recipe.dto.RecInfoDTO;
import xiaozhi.modules.recipe.entity.RecInfoEntity;
import xiaozhi.modules.recipe.service.RecClassifyService;
import xiaozhi.modules.recipe.service.RecInfoService;
import xiaozhi.modules.recipe.service.RecProcessService;
import xiaozhi.modules.recipe.vo.RecInfoServerVO;
import xiaozhi.modules.recipe.vo.RecInfoVO;

import java.util.List;
import java.util.Map;

/**
 * 菜单信息管理
 */
@RestController
@RequestMapping("/recipe/info")
@Tag(name = "菜单信息管理")
@AllArgsConstructor
public class RecInfoController {
    private final RecInfoService recInfoService;

    @GetMapping("/page")
    @Operation(operationId = "分页查询")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true)
    })
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<PageData<RecInfoVO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {

        Page<RecInfoEntity> page = recInfoService.getPage(params);
        return new Result<PageData<RecInfoVO>>().ok(new PageData<RecInfoVO>(recInfoService.toVoList(page.getRecords()), page.getTotal()));
    }

    @GetMapping("{id}")
    @Operation(operationId = "信息")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<RecInfoVO> get(@PathVariable("id") String id) {
        RecInfoVO recInfoVO = null;
        RecInfoEntity recInfoEntity = recInfoService.getById(id);
        if(recInfoEntity != null){
            List<RecInfoVO> data = recInfoService.toVoList(List.of(recInfoEntity));
            recInfoVO = data.getFirst();
        }
        return new Result<RecInfoVO>().ok(recInfoVO);
    }

    @PostMapping
    @Operation(operationId = "保存")
    @LogOperation("保存")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> save(@RequestBody RecInfoDTO dto) {
        recInfoService.add(dto);
        return new Result<Void>();
    }

    @PutMapping
    @Operation(operationId = "修改")
    @LogOperation("修改")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> update(@RequestBody RecInfoVO vo) {
        recInfoService.edit(vo);
        return new Result<Void>();
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "删除模型配置")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> delete(@PathVariable String id) {
        recInfoService.delete(id);
        return new Result<Void>();
    }

    @GetMapping("{device_mac}/{id}")
    @Operation(operationId = "发送菜谱")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<String> sendRecipe(@PathVariable("device_mac") String device_mac,@PathVariable("id") String id) {
        return new Result<String>().ok(recInfoService.sendRecipe(device_mac, id));
    }
}
