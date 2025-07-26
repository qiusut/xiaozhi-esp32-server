package xiaozhi.modules.tb.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
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
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.ApiUtils;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.tb.query.InvokingApi;
import xiaozhi.modules.tb.service.TbDeviceService;
import xiaozhi.modules.tb.vo.TbFunctionVO;

import java.util.List;
import java.util.Map;

/**
 * 接口调用
 *
 * @author qiust
 */
@RestController
@RequestMapping("tb/invoking")
@Tag(name = "tb系统接口调用")
public class InvokingController {

    @Resource
    private TbDeviceService tbDeviceService;

    @PostMapping("/tbHttp")
    @Operation(summary = "请求thingsBoard系统接口")
    public Result<JSON> tbHttp(@RequestBody InvokingApi invokingApi) {
        String result = "";
        String tbToken = "";
        List<String> tokens = tbDeviceService.initTbToken(invokingApi.getAgentId());
        Assert.isTrue(CollUtil.isNotEmpty(tokens),"未关联thingsBoard账号");
        tbToken = tokens.getFirst();
        Assert.isTrue(StringUtils.isNotBlank(tbToken),"Token为空");

        if(!invokingApi.getUrl().toLowerCase().startsWith("http")){
            invokingApi.setUrl(SpringUtil.getProperty("tb.url")+invokingApi.getUrl());
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
