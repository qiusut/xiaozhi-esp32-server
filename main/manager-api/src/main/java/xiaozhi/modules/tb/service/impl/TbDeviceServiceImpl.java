package xiaozhi.modules.tb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
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
import xiaozhi.common.redis.RedisUtils;
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
    private RedisUtils redisUtils;

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

    @Override
    public void initRedis(){
        Set<String> keys = redisUtils.getRedisTemplate().keys("tb:*");
        if (ObjectUtil.isNotEmpty(keys)) {
            redisUtils.getRedisTemplate().delete(keys);
        }

        redisUtils.setRawString("tb:url", sysParamsService.getValue("tb.url", true));
        redisUtils.setRawString("tb:name_desc", sysParamsService.getValue("tb.name_desc", true));

        List<TbFunctionEntity> list =this.list(Wrappers.lambdaQuery(TbFunctionEntity.class).eq(TbFunctionEntity::getStatus, 1));
        if(CollectionUtil.isNotEmpty(list)) {
            for(TbFunctionEntity item : list){
                saveFunction(item);
            }
        }
    }

}