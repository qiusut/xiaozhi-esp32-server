package xiaozhi.modules.tb.controller;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xiaozhi.common.utils.ApiUtils;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.tb.query.InvokingApi;

import java.util.Map;

/**
 * 接口调用
 *
 * @author qiust
 */
@RestController
@RequestMapping("tb/invoking")
@Tag(name = "接口调用")
public class InvokingController {


    /*@PostMapping("/tbHttp")
    @Operation(summary = "请求thingsBoard系统接口")
    //@OperateLog(type = OperateTypeEnum.OTHER)
    public Result<JSON> tbHttp(@RequestBody InvokingApi invokingApi) {
        String result = "";
        String tbToken = "";
        UserDetail user = SecurityUser.getUser();

        if(StringUtils.isNotBlank(invokingApi.getGoviewUUID())&&invokingApi.getTenantId()!=null){
            String key = RedisCache.GOVIEW_TOKEN_KEY.replace("${tenantId}", String.valueOf(invokingApi.getTenantId()));
            Map<String, String> tbMap = (Map<String, String>) LocalCacheUtil.hGet(key, invokingApi.getGoviewUUID());
            if(tbMap!=null){
                tbToken = tbMap.get("token");
            }
        }

        if(StringUtils.isBlank(tbToken)){
            if(user!=null){
                tbToken = user.getTbToken();
            }
        }

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
        //System.out.println(result);
        JSON jsonResult = null;
        try {
            jsonResult = JSON.parseObject(result);
        } catch (Exception e) {
            try {
                jsonResult = JSON.parseArray(result);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return Result.ok(jsonResult);
    }*/

}
