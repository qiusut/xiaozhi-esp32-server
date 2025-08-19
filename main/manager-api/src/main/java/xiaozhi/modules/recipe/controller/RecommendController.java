package xiaozhi.modules.recipe.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import xiaozhi.modules.recipe.dto.RecommendDTO;
import xiaozhi.modules.recipe.dto.RecommendDTO;
import xiaozhi.modules.recipe.entity.RecActionEntity;
import xiaozhi.modules.recipe.entity.RecommendEntity;
import xiaozhi.modules.recipe.entity.RecommendEntity;
import xiaozhi.modules.recipe.service.RecommendService;
import xiaozhi.modules.recipe.service.RecommendService;
import xiaozhi.modules.recipe.vo.RecActionVO;
import xiaozhi.modules.recipe.vo.RecommendVO;
import xiaozhi.modules.security.user.SecurityUser;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 菜单推荐
 */
@RestController
@RequestMapping("/recipe/commend")
@Tag(name = "菜谱推荐")
@AllArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;

    @GetMapping("/page")
    @Operation(operationId = "菜谱推荐查询")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true)
    })
    //@RequiresPermissions("recipe:commend:page")
    public Result<PageData<RecommendVO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {

        Page<RecommendEntity> page = recommendService.getPage(params);
        return new Result<PageData<RecommendVO>>().ok(
                new PageData<RecommendVO>(
                        recommendService.toListVO(page.getRecords()), page.getTotal()
                )
        );
    }

    @GetMapping("{id}")
    @Operation(operationId = "推荐信息")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<RecommendDTO> get(@PathVariable("id") String id) {
        RecommendEntity recommendEntity = recommendService.getById(id);
        RecommendDTO data = BeanUtil.copyProperties(recommendEntity, RecommendDTO.class);
        return new Result<RecommendDTO>().ok(data);
    }

    /*@PostMapping("/add")
    @Operation(operationId = "推荐保存")
    @LogOperation("推荐保存")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> save(@RequestBody RecommendDTO dto) {
        RecommendEntity entity = BeanUtil.copyProperties(dto, RecommendEntity.class);
        recommendService.save(entity);
        return new Result<Void>();
    }*/

    @PostMapping("/reported")
    @Operation(operationId = "推荐上报")
    @LogOperation("推荐上报")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> reported(@RequestBody RecommendDTO dto) {
        recommendService.reported(dto);
        return new Result<Void>();
    }

    @PostMapping("/pass/{id}")
    @Operation(operationId = "推荐通过")
    @LogOperation("推荐通过")
    @RequiresPermissions("recipe:commend:pass")
    public Result<Void> pass(@PathVariable("id") String id,@RequestParam(required = false,name = "审核意见") String auditIdea) {

        recommendService.pass(id,auditIdea);

        return new Result<Void>();
    }

    @PostMapping("/reject/{id}")
    @Operation(operationId = "推荐驳回")
    @LogOperation("推荐驳回")
    @RequiresPermissions("recipe:commend:reject")
    public Result<Void> reject(@PathVariable(name = "id") String id,@RequestParam(required = false,name = "审核意见") String auditIdea) {

        recommendService.update(Wrappers.lambdaUpdate(RecommendEntity.class).eq(RecommendEntity::getId,id)
                        .eq(RecommendEntity::getAuditStatus,1)
                        .set(StrUtil.isNotBlank(auditIdea),RecommendEntity::getAuditIdea,auditIdea)
                        .set(RecommendEntity::getAuditor,SecurityUser.getUser().getId())
                        .set(RecommendEntity::getAuditDate,new Date())
                        .set(RecommendEntity::getAuditStatus,3));

        return new Result<Void>();
    }

    @PutMapping("/update")
    @Operation(operationId = "推荐修改")
    @LogOperation("推荐修改")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> update(@RequestBody RecommendVO vo) {

        RecommendEntity entity = BeanUtil.copyProperties(vo, RecommendEntity.class);
        recommendService.updateById(entity);

        return new Result<Void>();
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "删除")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<Void> delete(@PathVariable String id) {
        recommendService.removeById(id);
        return new Result<Void>();
    }
}
