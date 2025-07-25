package xiaozhi.modules.tb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.utils.ApiUtils;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.dao.AgentPluginMappingMapper;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.agent.entity.AgentPluginMapping;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.dao.SysUserDao;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.tb.dao.TbFunctionDao;
import xiaozhi.modules.tb.dto.TbDeviceRpcDTO;
import xiaozhi.modules.tb.dto.TbFunctionDTO;
import xiaozhi.modules.tb.entity.TbFunctionEntity;
import xiaozhi.modules.tb.query.DeviceInfoQuery;
import xiaozhi.modules.tb.query.InvokingApi;
import xiaozhi.modules.tb.service.TbDeviceService;
import xiaozhi.modules.tb.vo.TbFunctionVO;

import java.util.*;

@Service
public class TbDeviceServiceImpl extends ServiceImpl<TbFunctionDao, TbFunctionEntity> implements TbDeviceService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    private SysUserDao sysUserDao;

    @Resource
    private AgentDao agentDao;

    @Resource
    private AgentPluginMappingMapper agentPluginMappingMapper;

    private final String getLogin = "/api/auth/login";
    private final String getTenantDeviceInfos = "/api/tenant/deviceInfos";
    private final String getCustomerDeviceInfos = "/api/customer/{customerId}/deviceInfos";
    private final String getUser = "/api/auth/user";
    private final String rpc_url = "/api/rpc/oneway/{deviceId}";


    private final String function_call = """
                    {
                    	"type": "function",
                    	"function": {
                    		"name": "{name}",
                    		"description": "{description}",
                    		"parameters": {
                                "type": "object",
                                "properties": {
                                    "tb_args": {
                                        "type": "object",
                                        "properties": "{properties}",
                                        "required": "{required}"
                                    }
                                },
                                "required": [
                                    "tb_agrs"
                                ]
                            }
                    	}
                    }
            """;

    @Override
    public List<JSONObject> infoList(DeviceInfoQuery query){
        List<JSONObject> result = new ArrayList<>();
        InvokingApi invokingApi = new InvokingApi();
        //String result = "";
        List<String> tokens = this.initTbToken(query.getAgentId());
        if(CollUtil.isNotEmpty(tokens)){
            List<Object> deviceIds = new ArrayList<>();
            for (String token : tokens) {
                JSONObject invokingApiJson_user = new JSONObject();
                invokingApiJson_user.set("url", sysParamsService.getValue("tb.url", true)+getUser);
                invokingApiJson_user.set("headers",new JSONObject().set("Authorization","Bearer "+token));
                JSONObject jsonObject = JSONUtil.parseObj(ApiUtils.invokingHttpApi(invokingApiJson_user));
                if(!jsonObject.containsKey("authority")){
                    log.error("tb系统获取用户信息出错："+ jsonObject);
                    //return Result.error("未关联thingsBoard账号");
                }else {
                    String authority = jsonObject.getStr("authority");

                    String url = "";
                    if(authority.equals("TENANT_ADMIN")){
                        url=getTenantDeviceInfos;
                    }else if(authority.equals("CUSTOMER_USER")){
                        String customerId = jsonObject.getJSONObject("customerId").getStr("id");
                        url=getCustomerDeviceInfos.replace("{customerId}",customerId);
                    }
                    String param = "?page=0&pageSize=100";
                    /*if(StringUtils.isNotBlank(query.getOrder())){
                        param+="&sortProperty="+query.getOrder();
                    }
                    if(query.isAsc()){
                        param+="&sortOrder=ASC";
                    }else {
                        param+="&sortOrder=DESC";
                    }*/
                    if (StringUtils.isNotBlank(query.getType())){
                        param+="&type="+query.getType();
                    }
                    if (StringUtils.isNotBlank(query.getTextSearch())){
                        param+="&textSearch="+query.getTextSearch();
                    }

                    invokingApi.setUrl(url+param);

                    if(!invokingApi.getUrl().toLowerCase().startsWith("http")){
                        invokingApi.setUrl(sysParamsService.getValue("tb.url", true)+invokingApi.getUrl());
                    }
                    InvokingApi.Headers headers = invokingApi.getHeaders();
                    if(headers == null)headers = new InvokingApi.Headers();
                    if(StringUtils.isBlank(headers.getXAuthorization())){
                        headers.setAuthorization("Bearer "+token);
                    }
                    invokingApi.setHeaders(headers);
                    JSONObject invokingApiJson = JSONUtil.parseObj(invokingApi);
                    String api_result = ApiUtils.invokingHttpApi(invokingApiJson);
                    JSONObject jsonResult = JSONUtil.parseObj(api_result);
                    if(jsonResult.containsKey("data")){
                        List<JSONObject> dataList = jsonResult.getBeanList("data",JSONObject.class);
                        for(JSONObject json : dataList){
                            Object deviceId = json.get("id");
                            if(deviceIds.contains(deviceId)){
                                continue;
                            }
                            deviceIds.add(deviceId);
                            result.add(json);
                        }
                    }
                }

            }
        }
        return result;
    }


    @Override
    public Page<TbFunctionEntity> deviceTypeList(Integer curPage, Integer limit, String type, String name) {
        /*List<JSONObject> jsonList = null;
        long total = 0;

        List<Object> allDevices = redisTemplate.opsForList().range("tb:device", page-1, (long) page *limit);

        if (ObjectUtil.isNotEmpty(allDevices)) {
            jsonList = allDevices.stream().map(JSONUtil::parseObj).toList();
            total = redisTemplate.opsForList().size("tb:device");
        }*/
        Page<TbFunctionEntity> page = new Page<>(curPage, limit);
        QueryWrapper<TbFunctionEntity> queryWrapper = Wrappers.query();
        queryWrapper.lambda().eq(StrUtil.isNotBlank(type),TbFunctionEntity::getType, type);
        queryWrapper.lambda().like(StrUtil.isNotBlank(name),TbFunctionEntity::getName, name);
        this.page(page);

        return page;
    }

    @Override
    public void addFunction(TbFunctionDTO tbFunctionDTO) {
        String type = tbFunctionDTO.getType();
        Assert.isFalse(this.exists(Wrappers.lambdaQuery(TbFunctionEntity.class).eq(TbFunctionEntity::getType, type)), "设备类型已存在");
        TbFunctionEntity entity = BeanUtil.toBean(tbFunctionDTO, TbFunctionEntity.class);
        this.save(entity);
        initRedis();
    }

    @Override
    public void updateFunction(TbFunctionVO tbFunctionVO) {
        String now_type = tbFunctionVO.getType();
        TbFunctionEntity tbFunction = this.getById(tbFunctionVO.getId());
        Assert.notNull(tbFunction, "设备不存在");
        String old_type = tbFunction.getType();
        if(!ObjectUtil.equal(old_type, now_type)){
            Assert.isFalse(this.exists(Wrappers.lambdaQuery(TbFunctionEntity.class).eq(TbFunctionEntity::getType, now_type)), "设备类型已存在");
        }

        this.updateById(BeanUtil.toBean(tbFunctionVO, TbFunctionEntity.class));
        initRedis();
    }

    @Override
    public List<String> initTbToken(String agentId){
        List<String> tokens = new ArrayList<>();
        try {
            LambdaQueryWrapper<AgentEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AgentEntity::getUserId, SecurityUser.getUserId());
            wrapper.eq(agentId!=null,AgentEntity::getId,agentId);
            List<AgentEntity> agents = agentDao.selectList(wrapper);

            if(CollUtil.isNotEmpty(agents)){
                List<Object> pluginMappings = agentPluginMappingMapper.selectObjs(Wrappers.lambdaQuery(AgentPluginMapping.class)
                                .eq(AgentPluginMapping::getPluginId, "SYSTEM_PLUGIN_TB_DEVICE")
                                .in(AgentPluginMapping::getAgentId, agents.stream().map(AgentEntity::getId).toList())
                                .select(AgentPluginMapping::getParamInfo)
                );

                if(CollUtil.isNotEmpty(pluginMappings)){
                    List<JSONObject> pluginMappingJson = pluginMappings.stream().distinct().map(JSONUtil::parseObj).toList();

                    for(JSONObject infoJson:pluginMappingJson){
                        String tb_username = infoJson.getStr("tb_username");
                        String tb_password = infoJson.getStr("tb_password");
                        //添加tb系统的token
                        if(StringUtils.isNotBlank(tb_username)){
                            String token = redisUtils.getRawStr("tb:account:"+tb_username+":token");
                            if(StringUtils.isBlank(token)){
                                try {
                                    JSONObject invokingApi = new JSONObject();
                                    invokingApi.set("url", sysParamsService.getValue("tb.url", true)+getLogin);
                                    invokingApi.set("method","POST");
                                    JSONObject bodyJsonObject = new JSONObject();
                                    bodyJsonObject.set("username", tb_username);
                                    bodyJsonObject.set("password", tb_password);
                                    invokingApi.set("body",bodyJsonObject);
                                    JSONObject jsonObject = JSONUtil.parseObj(ApiUtils.invokingHttpApi(invokingApi));
                                    if(jsonObject.containsKey("token")){
                                        token = jsonObject.getStr("token");
                                        redisUtils.setRawStr("tb:account:"+tb_username+":token", token, RedisUtils.HOUR_ONE_EXPIRE);
                                    }else {
                                        log.error("tb系统登录失败："+jsonObject);
                                    }

                                } catch (Exception e) {
                                    log.error("获取tb系统token失败请联系管理员"+e.getMessage());
                                }
                            }
                            if(StringUtils.isNotBlank(token)){
                                tokens.add(token);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("tb系统token初始化 error ", e);
        }
        return tokens;
    }


    @Override
    public String sendDeviceRpc(TbDeviceRpcDTO tbDeviceRpc){
        List<String> tokens = initTbToken(tbDeviceRpc.getAgentId());
        Assert.isTrue(CollUtil.isNotEmpty(tokens), "该智能体未配置账号");
        JSONObject invokingApi = new JSONObject();
        invokingApi.set("headers",new JSONObject().set("Authorization","Bearer "+tokens.getFirst()));
        invokingApi.set("url", sysParamsService.getValue("tb.url", true)+rpc_url.replace("{deviceId}",tbDeviceRpc.getDeviceId()));
        invokingApi.set("method","POST");
        JSONObject bodyJsonObject = new JSONObject();
        bodyJsonObject.set("method", tbDeviceRpc.getMethod());
        bodyJsonObject.set("params", tbDeviceRpc.getParams());
        invokingApi.set("body",bodyJsonObject);
        String result = ApiUtils.invokingHttpApi(invokingApi);
        log.debug("tb系统执行rpc："+result);
        return result;
    }

    @Override
    public void initRedis(){
        Set<String> keys = redisUtils.getRedisTemplate().keys("tb:*");
        if (ObjectUtil.isNotEmpty(keys)) {
            redisUtils.getRedisTemplate().delete(keys);
        }

        redisUtils.setRawStr("tb:url", sysParamsService.getValue("tb.url", true),null);
        redisUtils.setRawStr("tb:name_desc", sysParamsService.getValue("tb.name_desc", true),null);

        List<TbFunctionEntity> list =this.list(Wrappers.lambdaQuery(TbFunctionEntity.class).eq(TbFunctionEntity::getStatus, 1));
        if(CollectionUtil.isNotEmpty(list)) {
            for(TbFunctionEntity item : list){
                saveFunction(item);
            }
        }
    }

    private void saveFunction(TbFunctionEntity tbFunction) {

        String type = tbFunction.getType();

        JSONObject deviceJson = new JSONObject();
        List<String> funs = new ArrayList<>();
        deviceJson.set("type", type);
        deviceJson.set("name", tbFunction.getName());

        for (TbFunctionEntity.Function_call function_call : tbFunction.getFuns()) {
            String random = RandomUtil.randomString(5).toLowerCase();

            funs.add(random);

            Map<String, Object> jsonObject_fun = new HashMap<>();
            jsonObject_fun.put("method", function_call.getMethodName());
            String function_call_tmp = this.function_call.replace("{name}", "tb_"+type+"_"+random)
                    .replace("{description}", function_call.getDescription());
            JSONObject function_call_json = JSONUtil.parseObj(function_call_tmp);
            JSONObject parameters_tb_args_json = function_call_json.getJSONObject("function").getJSONObject("parameters").getJSONObject("properties").getJSONObject("tb_args");

            List<String> required_json = new ArrayList<>();
            JSONObject properties_json = new JSONObject();
            List<TbFunctionEntity.Function_call.Function_call_params> params = function_call.getParams();
            if(ObjectUtil.isNotEmpty(params)){
                for (TbFunctionEntity.Function_call.Function_call_params param : params) {

                    String paramDesc = param.getDescription();
                    properties_json.set(param.getName(), JSONUtil.parseObj("""
                            {
                                "type": "{paramType}",
                                "description": "{paramDesc}"
                            }
                            """.replace("{paramType}", param.getType()).replace("{paramDesc}", paramDesc)));

                    if(ObjectUtil.equal(param.getIs_required(), 1)){
                        required_json.add(param.getName());
                    }
                }
            }
            parameters_tb_args_json.set("properties", properties_json);
            parameters_tb_args_json.set("required", required_json);
            jsonObject_fun.put("function_call", function_call_json);
            redisUtils.rawHashPutAll("tb:device_fun:"+type+":"+random, jsonObject_fun);
        }
        deviceJson.set("funs", funs);
        redisUtils.rawRightPush("tb:device", deviceJson.toString());

    }

}