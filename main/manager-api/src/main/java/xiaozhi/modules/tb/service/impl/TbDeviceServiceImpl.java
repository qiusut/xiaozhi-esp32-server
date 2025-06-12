package xiaozhi.modules.tb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import xiaozhi.modules.sys.dao.SysUserDao;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.tb.dao.TbFunctionDao;
import xiaozhi.modules.tb.dto.TbFunctionDTO;
import xiaozhi.modules.tb.entity.TbFunctionEntity;
import xiaozhi.modules.tb.service.TbDeviceService;
import xiaozhi.modules.tb.vo.TbFunctionVO;

import java.util.*;

@Service
public class TbDeviceServiceImpl extends ServiceImpl<TbFunctionDao, TbFunctionEntity> implements TbDeviceService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    private SysUserDao sysUserDao;


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
        TbFunctionEntity entity = BeanUtil.toBean(tbFunctionDTO, TbFunctionEntity.class);
        this.save(entity);
        initRedis();

        /*redisTemplate.multi();
        try {
            //String type = tbFunctionDTO.getType();
            List<Object> allDevices = redisTemplate.opsForList().range("tb:device", 0, -1);
            if (allDevices != null) {
                Assert.isFalse(allDevices.stream().anyMatch(device -> {
                    JSONObject deviceJson = JSONUtil.parseObj(device);
                    return deviceJson.getStr("type").equals(tbFunctionDTO.getType());
                }), "设备类型已存在");
            }

            this.saveFunction(tbFunctionDTO);
            // 提交事务
            redisTemplate.exec();
        } catch (Exception e) {
            // 回滚事务
            redisTemplate.discard();
            throw e;
        }*/

    }

    @Override
    public void updateFunction(TbFunctionVO tbFunctionVO) {
        this.updateById(BeanUtil.toBean(tbFunctionVO, TbFunctionEntity.class));
        initRedis();
        /*redisTemplate.multi();
        try {
            String updateType = tbFunctionVO.getType();
            List<Object> allDevices = redisTemplate.opsForList().range("tb:device", 0, -1);
            JSONObject jsonObject = allDevices.stream()
                    .map(JSONUtil::parseObj)
                    .filter(deviceJson -> type.equals(deviceJson.getStr("type")))
                    .findFirst()
                    .orElse(null);

            Assert.notNull(jsonObject, "设备类型不存在");
            if(updateType.equals(type)){
                List<String> oldFuns = jsonObject.getBeanList("funs", String.class);
                oldFuns.stream().forEach(e -> {
                    redisTemplate.opsForHash().delete("tb:device_fun:"+type+":"+e);
                });

                this.saveFunction(BeanUtil.toBean(tbFunctionVO, TbFunctionDTO.class));

            }else {
                Assert.isFalse(allDevices.stream().anyMatch(device -> {
                    JSONObject deviceJson = JSONUtil.parseObj(device);
                    return updateType.equals(deviceJson.getStr("type"));
                }), "设备类型已存在");

                this.saveFunction(BeanUtil.toBean(tbFunctionVO, TbFunctionDTO.class));
            }
            // 提交事务
            redisTemplate.exec();
        } catch (Exception e) {
            // 回滚事务
            redisTemplate.discard();
            throw e;
        }*/
    }

    private void saveFunction(TbFunctionEntity tbFunction) {

        String type = tbFunction.getType();

        JSONObject deviceJson = new JSONObject();
        List<String> funs = new ArrayList<>();
        deviceJson.set("type", type);
        deviceJson.set("name", tbFunction.getName());

        for (TbFunctionEntity.Function_call function_call : tbFunction.getFuns()) {
            String random = RandomUtil.randomStringUpper(5);

            funs.add(random);

            Map<String, Object> jsonObject_fun = new HashMap<>();
            jsonObject_fun.put("method", function_call.getMethodName());
            String function_call_tmp = this.function_call.replace("{name}", type+"_"+random)
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
            rawHashPutAll("tb:device_fun:"+type+":"+random, jsonObject_fun);
        }
        deviceJson.set("funs", funs);
        rawRightPush("tb:device", deviceJson.toString());

    }

    @Override
    public void initRedis(){
        Set<String> keys = redisTemplate.keys("tb:*");
        if (ObjectUtil.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }

        setRawString("tb:url", sysParamsService.getValue("tb.url", true));
        setRawString("tb:name_desc", sysParamsService.getValue("tb.name_desc", true));

        List<SysUserEntity> sysUserList = sysUserDao.selectList(
                Wrappers.<SysUserEntity>lambdaQuery()
                        .isNotNull(SysUserEntity::getTbUsername)
                        .ne(SysUserEntity::getTbUsername, "")
                        .isNotNull(SysUserEntity::getTbPassword)
                        .ne(SysUserEntity::getTbPassword, "")
        );

        sysUserList.stream().forEach(sysUser -> {
            setRawString("tb:user:"+sysUser.getId()+":username",sysUser.getTbUsername());
            setRawString("tb:user:"+sysUser.getId()+":password",sysUser.getTbPassword());
        });

        List<TbFunctionEntity> list =this.list(Wrappers.lambdaQuery(TbFunctionEntity.class).eq(TbFunctionEntity::getStatus, 1));
        if(CollectionUtil.isNotEmpty(list)) {
            for(TbFunctionEntity item : list){
                saveFunction(item);
            }
        }
    }

    public void setRawString(String key, String value) {
        redisTemplate.execute((RedisConnection connection) -> {
            byte[] keyBytes = key.getBytes();
            byte[] valueBytes = value.getBytes();
            connection.stringCommands().set(keyBytes, valueBytes);
            return null;
        });
    }

    public void rawRightPush(String key, String value) {
        redisTemplate.execute((RedisConnection connection) -> {
            byte[] keyBytes = key.getBytes();
            byte[] valueBytes = value.getBytes();
            connection.listCommands().rPush(keyBytes, valueBytes);
            return null;
        });
    }

    public void rawHashPutAll(String key, Map<String, Object> hashEntries) {
        redisTemplate.execute((RedisConnection connection) -> {
            byte[] keyBytes = key.getBytes();
            for (Map.Entry<String, Object> entry : hashEntries.entrySet()) {
                byte[] fieldBytes = entry.getKey().getBytes();
                byte[] valueBytes = JSONUtil.toJsonStr(entry.getValue()).getBytes();
                connection.hashCommands().hSet(keyBytes, fieldBytes, valueBytes);
            }
            return null;
        });
    }





}