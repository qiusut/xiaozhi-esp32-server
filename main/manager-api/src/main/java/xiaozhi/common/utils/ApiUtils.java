package xiaozhi.common.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 调用api
 *
 * @author qiust
 */
@Slf4j
public class ApiUtils {

    public static String invokingHttpApi(JSONObject invokingApiJson){
        //log.info("调用invokingApi接口请求参数api:{}",invokingApiJson.toString());
        // 使用headers来发起HTTP请求
        String result = null;
        Map<String, String> headersMap = new HashMap<>();
        JSONObject headers = invokingApiJson.getJSONObject("headers");
        if(ObjectUtil.isNotEmpty(headers)){
            if(ObjectUtil.isNotEmpty(headers.getStr("contentType"))){
                headersMap.put("Content-Type", headers.getStr("contentType"));
            }
            if(ObjectUtil.isNotEmpty(headers.getStr("accept"))){
                headersMap.put("Accept", headers.getStr("accept"));
            }
            if(ObjectUtil.isNotEmpty(headers.getStr("acceptLanguage"))){
                headersMap.put("Accept-Language", headers.getStr("acceptLanguage"));
            }
            if(ObjectUtil.isNotEmpty(headers.getStr("acceptEncoding"))){
                headersMap.put("Accept-Encoding", headers.getStr("acceptEncoding"));
            }
            if(ObjectUtil.isNotEmpty(headers.getStr("Authorization"))){
                headersMap.put("Authorization", headers.getStr("Authorization"));
            }
            if(ObjectUtil.isNotEmpty(headers.getStr("X-Authorization"))){
                headersMap.put("X-Authorization", headers.getStr("X-Authorization"));
            }
        }

        //log.info("调用invokingApi接口请求头headersMap:{}",headersMap);

        //.form()专门用于发送表单数据，即 application/x-www-form-urlencoded 类型的数据。.body() 可以替代 .form()，但需要手动设置内容类型,例如 .header("Content-Type", "application/json").body(jsonBody)
        try {
            HttpResponse response = HttpUtil.createRequest(Method.valueOf(invokingApiJson.getStr("method","GET")), invokingApiJson.getStr("url"))
                    .addHeaders(headersMap)
                    .body(invokingApiJson.getStr("body"))
                    .execute();
            //log.info("调用invokingApi接口返回结果result:{}",result);
            try (response) {
                result = response.body();
            }
        } catch (Exception e) {
            log.error("调用invokingApi接口异常:",e);
            result = "error:"+e.getMessage();
        }
        return result;
    }
}
