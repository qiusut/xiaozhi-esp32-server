package xiaozhi.modules.tb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.ApiUtils;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.enums.SuperAdminEnum;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.tb.dao.TbDeviceDao;
import xiaozhi.modules.tb.dto.TbDeviceDTO;
import xiaozhi.modules.tb.dto.TbDeviceRpcDTO;
import xiaozhi.modules.tb.entity.TbDeviceEntity;
import xiaozhi.modules.tb.query.DeviceQueryPage;
import xiaozhi.modules.tb.service.TbDeviceService;
import xiaozhi.modules.tb.service.TbFunctionService;
import xiaozhi.modules.tb.vo.TbDeviceVO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TbDeviceServiceImpl extends ServiceImpl<TbDeviceDao, TbDeviceEntity> implements TbDeviceService {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    @Lazy
    private TbFunctionService tbFunctionService;

    private final String getLogin = "/api/auth/login";
    private final String getTenantDeviceInfos = "/api/tenant/deviceInfos";
    private final String getCustomerDeviceInfos = "/api/customer/{customerId}/deviceInfos";
    private final String getUser = "/api/auth/user";
    private final String rpc_url = "/api/rpc/oneway/{deviceId}";
    private final String getDeviceInfoById = "/api/device/info/{deviceId}";
    private final String getDevicesByIds = "/api/devices?deviceIds={deviceIds}";


    @Override
    public TbDeviceVO getTbDevice(String id){
        TbDeviceEntity tbDeviceEntity = this.getById(id);
        JSONObject tbJson = getTbDeviceJson(tbDeviceEntity.getTbDeviceId());
        TbDeviceVO tbDeviceVO = BeanUtil.copyProperties(tbDeviceEntity, TbDeviceVO.class);
        tbDeviceVO.setEntityType(tbJson.getJSONObject("id").getStr("entityType"));
        tbDeviceVO.setActive(tbJson.getBool("active",false));
        return tbDeviceVO;
    }


    @Override
    public JSONObject getTbDeviceJson(String tbDeviceId){
        JSONObject resultJson = new JSONObject();
        JSONObject invokingApiJson_user = new JSONObject();
        String url = sysParamsService.getValue("tb.url", true)+getDeviceInfoById.replace("{deviceId}",tbDeviceId);
        invokingApiJson_user.set("url", url);
        invokingApiJson_user.set("headers",new JSONObject().set("Authorization","Bearer "+initTbToken(false)));
        String result = ApiUtils.invokingHttpApi(invokingApiJson_user);
        if(JSONUtil.isTypeJSONObject(result)){
            resultJson = JSONUtil.parseObj(result);
        }else {
            resultJson.set("error", result);
        }
        return resultJson;
    }

    public JSONArray getTbDeviceJson(List<String> tbDeviceIds){
        JSONArray resultJson = new JSONArray();
        JSONObject invokingApiJson_user = new JSONObject();
        String url = sysParamsService.getValue("tb.url", true)+getDevicesByIds.replace("{deviceIds}",String.join(",",tbDeviceIds));
        invokingApiJson_user.set("url", url);
        invokingApiJson_user.set("headers",new JSONObject().set("Authorization","Bearer "+initTbToken(false)));
        String result = ApiUtils.invokingHttpApi(invokingApiJson_user);
        if(JSONUtil.isTypeJSONArray(result)){
            resultJson = JSONUtil.parseArray(result);
        }
        return resultJson;
    }


    @Override
    public List<JSONObject> getTbDeviceList(){
        List<JSONObject> resultJson = new ArrayList<>();
        List<TbDeviceEntity> list = this.list(Wrappers.lambdaQuery(TbDeviceEntity.class)
                        .eq(TbDeviceEntity::getStatus, 1)
                        .eq(TbDeviceEntity::getUserId,SecurityUser.getUserId())
                );
        if(CollectionUtil.isNotEmpty(list)){
            for(TbDeviceEntity tbDeviceEntity:list){
                JSONObject tbJson = new JSONObject();
                resultJson.add(tbJson);
                tbJson.set("id", tbDeviceEntity.getId());
                tbJson.set("name", tbDeviceEntity.getName());
                tbJson.set("type", tbDeviceEntity.getType());
                tbJson.set("tbDeviceId", tbDeviceEntity.getTbDeviceId());
                JSONObject tbApiJson = getTbDeviceJson(tbDeviceEntity.getTbDeviceId());
                if(ObjectUtil.isNotNull(tbApiJson)){
                    tbJson.set("isActive", tbApiJson.getBool("active",false)?1:0);
                }
            }
        }
        return resultJson;
    }

    @Override
    public Page<TbDeviceEntity> getPage(DeviceQueryPage deviceQueryPage) {
        UserDetail user = SecurityUser.getUser();
        Page<TbDeviceEntity> page = new Page<>(deviceQueryPage.getPage(), deviceQueryPage.getLimit());
        LambdaQueryWrapper<TbDeviceEntity> queryWrapper = Wrappers.lambdaQuery(TbDeviceEntity.class);
        queryWrapper.like(StrUtil.isNotBlank(deviceQueryPage.getName()), TbDeviceEntity::getName, deviceQueryPage.getName());
        queryWrapper.eq(StrUtil.isNotBlank(deviceQueryPage.getType()), TbDeviceEntity::getType, deviceQueryPage.getType());
        if(user.getSuperAdmin().equals(SuperAdminEnum.YES.value())){
            queryWrapper.eq(StrUtil.isNotBlank(deviceQueryPage.getUsername()), TbDeviceEntity::getUsername, user.getUsername());
        }else {
            queryWrapper.eq(TbDeviceEntity::getUserId, user.getId());
        }

        return this.page(page, queryWrapper);
    }


    @Override
    public void addTbDevice(TbDeviceDTO tbDeviceDTO){

        boolean isExist = this.exists(Wrappers.lambdaQuery(TbDeviceEntity.class).eq(TbDeviceEntity::getTbDeviceId, tbDeviceDTO.getTbDeviceId()));
        Assert.isFalse(isExist, "该设备已被绑定");

        JSONObject deviceInfoJson = getTbDeviceJson(tbDeviceDTO.getTbDeviceId());
        Assert.isTrue(deviceInfoJson.containsKey("type"), "该设备不存在");

        UserDetail user = SecurityUser.getUser();

        String name = tbDeviceDTO.getName();
        boolean isNameExist = this.exists(Wrappers.lambdaQuery(TbDeviceEntity.class).eq(TbDeviceEntity::getName, name).eq(TbDeviceEntity::getUserId, user.getId()));
        Assert.isFalse(isNameExist, "该名称已在你的设备列表存在请重新命名");

        TbDeviceEntity tbDeviceEntity = new TbDeviceEntity();

        tbDeviceEntity.setUsername(user.getUsername());
        tbDeviceEntity.setTbDeviceId(tbDeviceDTO.getTbDeviceId());
        tbDeviceEntity.setName(tbDeviceDTO.getName());
        tbDeviceEntity.setType(deviceInfoJson.getStr("type"));

        this.save(tbDeviceEntity);

        initTbDeviceRedis(user.getId());
    }

    @Override
    public void deleteTbDevice(String id){
        this.removeById(id);
        initTbDeviceRedis(SecurityUser.getUserId());
    }

    @Override
    public String initTbToken(boolean isRefresh){
        String token = redisUtils.getRawStr("tb:token");
        if(!isRefresh && StringUtils.isNotBlank(token)){
            return token;
        }
        String tb_username = sysParamsService.getValue("tb.username", true);
        String tb_password = sysParamsService.getValue("tb.password", true);
        //添加tb系统的token
        if(StringUtils.isNotBlank(tb_username)){

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
                    redisUtils.setRawStr("tb:token", token, RedisUtils.HOUR_ONE_EXPIRE);
                }else {
                    log.error("tb系统登录失败："+jsonObject);
                }

            } catch (Exception e) {
                log.error("获取tb系统token失败请联系管理员"+e.getMessage());
            }
        }
        return token;
    }


    @Override
    public String sendDeviceRpc(TbDeviceRpcDTO tbDeviceRpc){
        String token = this.initTbToken(false);
        Assert.isTrue(StrUtil.isNotBlank(token), "该智能体未配置账号");
        JSONObject invokingApi = new JSONObject();
        invokingApi.set("headers",new JSONObject().set("Authorization","Bearer "+token));
        invokingApi.set("url", sysParamsService.getValue("tb.url", true)+rpc_url.replace("{deviceId}",tbDeviceRpc.getTbDeviceId()));
        invokingApi.set("method","POST");
        JSONObject bodyJsonObject = new JSONObject();
        bodyJsonObject.set("method", tbDeviceRpc.getMethod());
        bodyJsonObject.set("params", tbDeviceRpc.getParams());
        invokingApi.set("body",bodyJsonObject);
        String result = ApiUtils.invokingHttpApi(invokingApi);
        log.debug("tb系统执行rpc："+result);
        return result;
    }



    @Scheduled(fixedRate = 25 * 60 * 1000)
    public void tokenTimer(){
        log.debug("tb系统token定时刷新");
        this.initTbToken(true);
    }

    @Override
    public void initTbDeviceRedis(Long userId){
        String keyPrefix = "tb:user:";
        //String funKeyPrefix = "tb:device_fun:";
        List<TbDeviceEntity> list = this.list(Wrappers.lambdaQuery(TbDeviceEntity.class)
                .eq(TbDeviceEntity::getStatus, 1)
                .eq(userId!=null,TbDeviceEntity::getUserId, userId)
                .orderByAsc(TbDeviceEntity::getCreateDate)
        );
        if(userId==null){
            Set<String> keys = redisUtils.getRedisTemplate().keys(keyPrefix);
            if (ObjectUtil.isNotEmpty(keys)) {
                redisUtils.getRedisTemplate().delete(keys);
            }
        }
        if(CollectionUtil.isNotEmpty(list)){
            List<String> tbDeviceIds = list.stream().map(TbDeviceEntity::getTbDeviceId).toList();
            if(CollectionUtil.isNotEmpty(tbDeviceIds)){
                JSONArray tbDeviceJson = getTbDeviceJson(tbDeviceIds);
                Map<String, JSONObject> tbDeviceMap = tbDeviceJson.stream().map(JSONObject::new).collect(Collectors.toMap(tbDeviceJsonObject -> tbDeviceJsonObject.getJSONObject("id").getStr("id"), tbDeviceJsonObject -> tbDeviceJsonObject));
                Map<Long,List<TbDeviceEntity>> userMap = list.stream().collect(Collectors.groupingBy(TbDeviceEntity::getUserId));
                for (Map.Entry<Long, List<TbDeviceEntity>> entry : userMap.entrySet()) {
                    JSONObject jsonObject_nameInfo = new JSONObject();
                    JSONObject jsonObject_funCall = new JSONObject();
                    List<String> names = new ArrayList<>();
                    String redisKey = keyPrefix + entry.getKey()+":";
                    List<TbDeviceEntity> devices = entry.getValue();
                    for(int i=0;i<devices.size();i++){
                        TbDeviceEntity item = devices.get(i);
                        jsonObject_nameInfo.set(item.getName(), tbDeviceMap.get(item.getTbDeviceId()));
                        names.add(item.getName());

                        //
                        JSONObject jsonObject_funCall_item = new JSONObject();
                        jsonObject_funCall_item.set("id", item.getTbDeviceId());
                        jsonObject_funCall_item.set("name", item.getName());
                        jsonObject_funCall_item.set("type", item.getType());
                        jsonObject_funCall.set("d"+i, jsonObject_funCall_item);
                    }
                    //redisUtils.setRawStr(redisKey+"names",JSONUtil.toJsonStr(names),null);

                    /*if(!jsonObject_nameInfo.isEmpty()){
                        redisUtils.getRedisTemplate().delete(redisKey + "name_info");
                        redisUtils.rawHashPutAll(redisKey + "name_info", jsonObject_nameInfo);
                    }*/
                    if(!jsonObject_funCall.isEmpty()){
                        redisUtils.getRedisTemplate().delete(redisKey + "fun_call");
                        redisUtils.rawHashPutAll(redisKey + "fun_call", jsonObject_funCall);
                    }
                }
            }
        }
    }

    @Override
    public void initRedis(){
        this.initTbDeviceRedis(null);
        tbFunctionService.initFunctionRedis();
    }

}