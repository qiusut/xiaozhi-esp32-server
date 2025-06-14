package xiaozhi.modules.recipe.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.common.constant.Constant;
import xiaozhi.common.redis.RedisUtils;
import xiaozhi.common.user.UserDetail;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.device.dao.DeviceDao;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.recipe.dao.RecInfoDao;
import xiaozhi.modules.recipe.dao.RecommendDao;
import xiaozhi.modules.recipe.dto.RecInfoDTO;
import xiaozhi.modules.recipe.dto.RecProcessDTO;
import xiaozhi.modules.recipe.entity.*;
import xiaozhi.modules.recipe.service.RecActionService;
import xiaozhi.modules.recipe.service.RecClassifyService;
import xiaozhi.modules.recipe.service.RecInfoService;
import xiaozhi.modules.recipe.service.RecProcessService;
import xiaozhi.modules.recipe.vo.RecInfoServerVO;
import xiaozhi.modules.recipe.vo.RecInfoVO;
import xiaozhi.modules.recipe.vo.RecProcessVO;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.service.SysParamsService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecInfoServiceImpl extends ServiceImpl<RecInfoDao, RecInfoEntity> implements RecInfoService {

    @Resource
    private RecProcessService recProcessService;

    @Resource
    private RecClassifyService recClassifyService;

    @Resource
    private RecActionService recActionService;

    @Resource
    private SysParamsService sysParamsService;

    @Resource
    private AgentDao agentDao;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private RecommendDao recommendDao;

    @Resource
    private RedisUtils redisUtils;

    @Value("${spring.profiles.active}")
    private String profiles_active;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(RecInfoDTO dto){
        String name =  dto.getName();
        LambdaQueryWrapper<RecInfoEntity> queryWrapper = Wrappers.lambdaQuery(RecInfoEntity.class);
        queryWrapper.eq(RecInfoEntity::getName, name);
        if(dto.getScope().equals(1)){
            queryWrapper.eq(RecInfoEntity::getScope,  1);
            queryWrapper.eq(RecInfoEntity::getUserId, SecurityUser.getUser().getId());
        }else {
            queryWrapper.eq(RecInfoEntity::getScope, 0);
        }
        boolean exists = this.exists(queryWrapper);
        Assert.isFalse(exists, "菜谱名称已存在");

        RecInfoEntity entity = BeanUtil.copyProperties(dto, RecInfoEntity.class);
        if(ObjectUtil.equals(entity.getScope(), 1)){
            entity.setUserId(SecurityUser.getUser().getId());
        }
        this.save(entity);
        List<RecProcessDTO> processDTOS = dto.getProcessDTOS();
        if(CollectionUtil.isNotEmpty(processDTOS)){
            List<RecProcessEntity> processEntities = BeanUtil.copyToList(processDTOS, RecProcessEntity.class);
            int sort = 0;
            for(RecProcessEntity item:processEntities){
                item.setInfoId(entity.getId());
                item.setSort(++sort);
            }
            recProcessService.saveBatch(processEntities);
        }
        initRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(RecInfoVO vo){
        RecInfoEntity entity = this.getById(vo.getId());
        if(!StrUtil.equals(entity.getName(), vo.getName())){
            LambdaQueryWrapper<RecInfoEntity> queryWrapper = Wrappers.lambdaQuery(RecInfoEntity.class);
            queryWrapper.eq(RecInfoEntity::getName, vo.getName());
            if(vo.getScope().equals(1)){
                queryWrapper.eq(RecInfoEntity::getScope,  1);
                queryWrapper.eq(RecInfoEntity::getUserId, SecurityUser.getUser().getId());
            }else {
                queryWrapper.eq(RecInfoEntity::getScope, 0);
            }
            boolean exists = this.exists(queryWrapper);
            Assert.isFalse(exists, "菜谱名称已存在");
        }

        entity = BeanUtil.copyProperties(vo, RecInfoEntity.class);
        this.updateById(entity);
        recProcessService.remove(new QueryWrapper<RecProcessEntity>().lambda().eq(RecProcessEntity::getInfoId, entity.getId()));
        List<RecProcessVO> recProcessVOS = vo.getProcessVOS();
        if(CollectionUtil.isNotEmpty(recProcessVOS)){
            List<RecProcessEntity> processEntities = BeanUtil.copyToList(recProcessVOS, RecProcessEntity.class);
            String infoId = entity.getId();
            int sort = 0;
            for(RecProcessEntity item:processEntities){
                item.setInfoId(infoId);
                item.setSort(++sort);
            }
            recProcessService.saveBatch(processEntities);
        }
        initRedis();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id){
        this.removeById(id);
        recProcessService.remove(new QueryWrapper<RecProcessEntity>().lambda().eq(RecProcessEntity::getInfoId, id));
        initRedis();
    }

    public Page<RecInfoEntity> getPage(Map<String, Object> params){
        // 分页参数
        long curPage = 1;
        long limit = 10;

        if (params.get(Constant.PAGE) != null) {
            curPage = Long.parseLong((String) params.get(Constant.PAGE));
        }
        if (params.get(Constant.LIMIT) != null) {
            limit = Long.parseLong((String) params.get(Constant.LIMIT));
        }
        QueryWrapper<RecInfoEntity> wrapper = new QueryWrapper<>();
        if(params.containsKey("name")){
            wrapper.lambda().like(ObjectUtil.isNotEmpty(params.get("name")),RecInfoEntity::getName, params.get("name"));
        }
        if(params.containsKey("classifyId")&&ObjectUtil.isNotEmpty(params.get("classifyId"))){
            String classifyId = (String) params.get("classifyId");
            List<String> ids = recClassifyService.listObjs(Wrappers.lambdaQuery(RecClassifyEntity.class).eq(RecClassifyEntity::getParentId, classifyId).select(RecClassifyEntity::getId));
            ids.add(classifyId);
            wrapper.lambda().in(RecInfoEntity::getClassifyId, ids);
        }

        if(params.containsKey("scope")){
            wrapper.lambda().eq(RecInfoEntity::getScope, params.get("scope"));
        }

        UserDetail user = SecurityUser.getUser();
        wrapper.lambda().and(e->{
            e.eq(RecInfoEntity::getUserId, user.getId()).or().eq(RecInfoEntity::getScope, 0);
        });

        wrapper.lambda().orderByAsc(RecInfoEntity::getScope).orderByDesc(RecInfoEntity::getCreateDate);

        Page<RecInfoEntity> page = new Page<>(curPage, limit);

        page = this.page(page, wrapper);

        return page;
    }


    @Override
    public List<RecInfoVO> toVoList(List<RecInfoEntity> list){
        List<RecInfoVO> recInfoVOS = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(list)){
            List<String> ids = new ArrayList<>();
            List<String> classifyIds = new ArrayList<>();
            list.forEach(item->{
                ids.add(item.getId());
                classifyIds.add(item.getClassifyId());
                recInfoVOS.add(BeanUtil.copyProperties(item, RecInfoVO.class));
            });
            Map<String, List<RecProcessEntity>> processMap = new HashMap<>();
            Map<String, String> classifyMap = new HashMap<>();
            Map<String, RecActionEntity> actionMap = new HashMap<>();
            List<RecProcessEntity> processList = recProcessService.list(new QueryWrapper<RecProcessEntity>().lambda().in(RecProcessEntity::getInfoId, ids).orderByAsc(RecProcessEntity::getSort));
            if(CollectionUtil.isNotEmpty(processList)){
                List<String> actionIds = new ArrayList<>();
                for(RecProcessEntity item:processList){
                    List<RecProcessEntity> recProcessEntityList = processMap.getOrDefault(item.getInfoId(), new ArrayList<>());
                    recProcessEntityList.add(item);
                    processMap.put(item.getInfoId(), recProcessEntityList);

                    List<RecProcessEntity.Action> actions = item.getActions();
                    if(CollectionUtil.isNotEmpty(actions)){
                        for(Object obj:actions){
                            if (obj instanceof Map<?, ?>) {
                                @SuppressWarnings("unchecked")
                                Map<String, String> map = (Map<String, String>) obj;
                                actionIds.add(map.get("id"));
                            }
                        }
                    }
                }


                if(CollectionUtil.isNotEmpty(actionIds)){
                    List<RecActionEntity> actionEntities = recActionService.list(Wrappers.lambdaQuery(RecActionEntity.class).in(RecActionEntity::getId, actionIds.stream().distinct().toList()));
                    if(CollectionUtil.isNotEmpty(actionEntities)){
                        actionMap = actionEntities.stream().collect(Collectors.toMap(RecActionEntity::getId,e->e));
                    }
                }
            }
            List<RecClassifyEntity> classifyList = recClassifyService.listByIds(classifyIds);
            if(CollectionUtil.isNotEmpty(classifyList)){
                classifyMap = classifyList.stream().collect(Collectors.toMap(RecClassifyEntity::getId, RecClassifyEntity::getName));
            }
            List<RecommendEntity> recommends = recommendDao.selectList(Wrappers.<RecommendEntity>lambdaQuery().in(RecommendEntity::getInfoId,ids).orderByDesc(RecommendEntity::getCreateDate));
            Map<String,Integer> commend_map = new HashMap<>();
            if (CollectionUtil.isNotEmpty(recommends)){
                commend_map = recommends.stream().collect(Collectors.toMap(RecommendEntity::getInfoId, RecommendEntity::getAuditStatus,(existing, replacement) -> existing));
            }
            for (RecInfoVO recInfoVO : recInfoVOS){
                recInfoVO.setClassifyName(classifyMap.get(recInfoVO.getClassifyId()));
                if(processMap.containsKey(recInfoVO.getId())){
                    List<RecProcessVO> processVOS = new ArrayList<>();
                    for (RecProcessEntity processEntity : processMap.get(recInfoVO.getId())) {
                        RecProcessVO processVO = BeanUtil.copyProperties(processEntity, RecProcessVO.class);
                        if(CollectionUtil.isNotEmpty(processEntity.getActions())){
                            for (RecProcessVO.Action action : processVO.getActions()){
                                if(actionMap.get(action.getId())!=null){
                                    action.setName(actionMap.get(action.getId()).getName());
                                }
                            }
                        }
                        processVOS.add(processVO);
                    }
                    recInfoVO.setProcessVOS(processVOS);
                }
                if(recInfoVO.getScope().equals(1)&&commend_map.containsKey(recInfoVO.getId())){
                    recInfoVO.setCommendStatus(commend_map.get(recInfoVO.getId()));
                }
            }
        }
        return recInfoVOS;
    }

    @Override
    public String sendRecipe(String device_mac, String id){
        String http_url = sysParamsService.getValue("server.http_url", true);
        String http_url_ws = sysParamsService.getValue("server.http_url_ws", true);
        if(StrUtil.equals("dev", profiles_active)){
            http_url = "http://127.0.0.1:8003";
        }
        RecInfoEntity dto = this.getById(id);
        Assert.notNull(dto,"菜谱不存在");
        Object info = redisUtils.getRedisTemplate().opsForHash().get("recipe:nameMap",dto.getName());
        Assert.notNull(info,"菜谱不存在");
        Map<String, String> headers = new HashMap<>();
        if(StrUtil.isNotBlank(device_mac)){
            headers.put("device_mac", device_mac);
        }

        // 发起 POST 请求
        return HttpUtil.createRequest(Method.POST, http_url + http_url_ws)
                .addHeaders(headers)
                .body("""
                        {"type": "recipe","recipe": ${recipe}}""".replace("${recipe}",info.toString()))
                .execute().body();
    }

    @Override
    public JSONObject getUserRecipe(String device_mac){
        JSONObject jsonObject = new JSONObject();
        DeviceEntity deviceEntity =deviceDao.selectById(device_mac);
        if(deviceEntity!=null){
            Long userId = deviceEntity.getUserId();
            List<RecInfoEntity> list = this.list(Wrappers.lambdaQuery(RecInfoEntity.class)
                    .eq(RecInfoEntity::getScope, 1)
                    .eq(RecInfoEntity::getStatus, 1)
                    .eq(RecInfoEntity::getUserId, userId));
            if (CollectionUtil.isNotEmpty(list)){
                List<RecInfoVO> recInfoVOS = this.toVoList(list);

                for (RecInfoVO item : recInfoVOS) {
                    RecInfoServerVO recInfoVO = BeanUtil.copyProperties(item, RecInfoServerVO.class);
                    jsonObject.set(item.getName(), recInfoVO);
                }
            }
        }
        return jsonObject;
    }


    @Override
    public void initRedis(){
        String key_prefix = "recipe:";
        List<RecInfoEntity> list =this.list(Wrappers.lambdaQuery(RecInfoEntity.class).eq(RecInfoEntity::getStatus, 1));
        if(CollectionUtil.isNotEmpty(list)) {
            List<RecInfoVO> recInfoVOS = this.toVoList(list);
            JSONObject jsonObject = new JSONObject();
            Map<Long,List<RecInfoVO>> userMap = new HashMap<>();

            for (RecInfoVO item : recInfoVOS) {
                if(item.getScope().equals(0)){
                    RecInfoServerVO recInfoVO = BeanUtil.copyProperties(item, RecInfoServerVO.class);
                    jsonObject.set(item.getName(), recInfoVO);
                }else {
                    List<RecInfoVO> recInfoVOList = userMap.getOrDefault(item.getUserId(), new ArrayList<>());
                    recInfoVOList.add(item);
                    userMap.put(item.getUserId(), recInfoVOList);
                }
            }
            if(!jsonObject.isEmpty()){
                redisUtils.getRedisTemplate().delete(key_prefix + "nameMap");
                redisUtils.rawHashPutAll(key_prefix + "nameMap", jsonObject);
            }
            if(!userMap.isEmpty()){
                for (Map.Entry<Long, List<RecInfoVO>> entry : userMap.entrySet()) {
                    JSONObject jsonObject1 = new JSONObject();
                    for (RecInfoVO item : entry.getValue()) {
                        RecInfoServerVO recInfoVO = BeanUtil.copyProperties(item, RecInfoServerVO.class);
                        jsonObject1.set(item.getName(), JSONUtil.toJsonStr(recInfoVO));
                    }
                    redisUtils.getRedisTemplate().delete(key_prefix + entry.getKey() + ":nameMap");
                    redisUtils.rawHashPutAll(key_prefix +"user:"+ entry.getKey() + ":nameMap", jsonObject1);
                }
            }
        }

        List<DeviceEntity> deviceEntityList = deviceDao.selectList(Wrappers.lambdaQuery(DeviceEntity.class));
        if (CollectionUtil.isNotEmpty(deviceEntityList)){
            for (DeviceEntity e : deviceEntityList){
                redisUtils.getRedisTemplate().opsForValue().set("device:"+e.getMacAddress().replace(":","-")+":user_id", e.getUserId());
            }
        }
    }


}
