package xiaozhi.modules.tb.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.page.PageData;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.tb.dto.TbDeviceRpcDTO;
import xiaozhi.modules.tb.dto.TbFunctionDTO;
import xiaozhi.modules.tb.entity.TbFunctionEntity;
import xiaozhi.modules.tb.service.TbDeviceService;
import xiaozhi.modules.tb.service.TbFunctionService;
import xiaozhi.modules.tb.vo.TbFunctionVO;

/**
 * xiaozhi-server tb配置获取
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("tb/function")
@Tag(name = "TB方法管理")
@AllArgsConstructor
public class TbFunctionController {
    private final TbDeviceService tbDeviceService;

    private final TbFunctionService tbFunctionService;

    /*@GetMapping("/infoList")
    @Operation(summary = "tb设备列表")
    public Result<List<JSONObject>> infoList(@ParameterObject @Valid DeviceInfoQuery query) {
        List<JSONObject> result = tbDeviceService.infoList(query);
        return new Result<List<JSONObject>>().ok(result);
    }*/

    @GetMapping("/deviceTypeList")
    @Operation(summary = "方法定义列表（管理员）")
    //@RequiresPermissions("sys:role:superAdmin")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
    })
    public Result<PageData<TbFunctionVO>> adminAgentList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = true, name = Constant.PAGE, defaultValue = "1") int curPage,
            @RequestParam(required = true, defaultValue = "10") int limit
    ) {
        Page<TbFunctionEntity> page = tbFunctionService.deviceTypeList(curPage, limit,type, name);
        PageData<TbFunctionVO> pageData = new PageData<>(BeanUtil.copyToList(page.getRecords(), TbFunctionVO.class),  page.getTotal());
        return new Result<PageData<TbFunctionVO>>().ok(pageData);
    }

    @GetMapping("{id}")
    @Operation(operationId = "信息")
    //@RequiresPermissions("sys:role:superAdmin")
    public Result<TbFunctionVO> get(@PathVariable("id") String id) {
        TbFunctionEntity tbFunctionEntity = tbFunctionService.getById(id);
        return new Result<TbFunctionVO>().ok(BeanUtil.toBean(tbFunctionEntity, TbFunctionVO.class));
    }

    @PostMapping("/addFunction")
    @Operation(summary = "新增")
    public Result<Void> addFunction(@RequestBody TbFunctionDTO tbFunctionDTO) {
        tbFunctionService.addFunction(tbFunctionDTO);
        return new Result<>();
    }

    @PutMapping("/updateFunction")
    @Operation(summary = "修改")
    public Result<Void> updateFunction(@RequestBody TbFunctionVO tbFunctionVO) {
        tbFunctionService.updateFunction(tbFunctionVO);
        return new Result<>();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除")
    //@RequiresPermissions("sys:role:normal")
    public Result<Void> delete(@PathVariable String id) {
        tbFunctionService.removeById(id);
        return new Result<>();
    }

}
