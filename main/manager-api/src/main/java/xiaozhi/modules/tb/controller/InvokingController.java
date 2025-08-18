package xiaozhi.modules.tb.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xiaozhi.common.utils.ApiUtils;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.tb.query.InvokingApi;
import xiaozhi.modules.tb.service.TbDeviceService;

/**
 * 接口调用
 *
 * @author qiust
 */
@RestController
@RequestMapping("tb/invoking")
@Tag(name = "tb系统接口调用(通用接口)")
public class InvokingController {

    @Resource
    private TbDeviceService tbDeviceService;

    @Resource
    private SysParamsService sysParamsService;

    @PostMapping("/tbHttp")
    @Operation(summary = "请求thingsBoard系统接口")
    public Result<JSON> tbHttp(@RequestBody InvokingApi invokingApi) {
        String result = "";
        String tbToken = tbDeviceService.initTbToken(false);
        Assert.isTrue(StringUtils.isNotBlank(tbToken),"Token为空");

        if(!invokingApi.getUrl().toLowerCase().startsWith("http")){
            invokingApi.setUrl(sysParamsService.getValue("tb.url", true)+"/"+invokingApi.getUrl());
        }
        InvokingApi.Headers headers = invokingApi.getHeaders();
        if(headers == null)headers = new InvokingApi.Headers();
        if(StringUtils.isBlank(headers.getXAuthorization())){
            headers.setAuthorization("Bearer "+tbToken);
        }
        invokingApi.setHeaders(headers);
        JSONObject invokingApiJson = JSONUtil.parseObj(invokingApi);
        result = ApiUtils.invokingHttpApi(invokingApiJson);
        JSON jsonResult = null;
        if (JSONUtil.isTypeJSON(result)) {
            jsonResult = JSONUtil.parse(result);
        } else {
            // 如果不是合法 JSON，创建一个包含错误信息的对象
            jsonResult = JSONUtil.createObj().set("error", result);
        }
        return new Result<JSON>().ok(jsonResult);
    }

}
