package xiaozhi.modules.recipe.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.common.constant.Constant;
import xiaozhi.modules.recipe.dao.RecommendDao;
import xiaozhi.modules.recipe.entity.RecInfoEntity;
import xiaozhi.modules.recipe.entity.RecProcessEntity;
import xiaozhi.modules.recipe.entity.RecommendEntity;
import xiaozhi.modules.recipe.service.RecInfoService;
import xiaozhi.modules.recipe.service.RecProcessService;
import xiaozhi.modules.recipe.service.RecommendService;
import xiaozhi.modules.recipe.vo.RecInfoVO;
import xiaozhi.modules.recipe.vo.RecommendVO;
import xiaozhi.modules.security.user.SecurityUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl extends ServiceImpl<RecommendDao, RecommendEntity> implements RecommendService {

    @Resource
    @Lazy
    private RecInfoService recInfoService;

    @Resource
    private RecProcessService recProcessService;

    public Page<RecommendEntity> getPage(Map<String, Object> params){
        // 分页参数
        long curPage = 1;
        long limit = 10;

        if (params.get(Constant.PAGE) != null) {
            curPage = Long.parseLong((String) params.get(Constant.PAGE));
        }
        if (params.get(Constant.LIMIT) != null) {
            limit = Long.parseLong((String) params.get(Constant.LIMIT));
        }
        QueryWrapper<RecommendEntity> wrapper = new QueryWrapper<>();
        if(params.containsKey("auditStatus")){
            wrapper.lambda().like(ObjectUtil.isNotEmpty(params.get("auditStatus")),RecommendEntity::getAuditStatus, params.get("auditStatus"));
        }
        wrapper.lambda().orderByDesc(RecommendEntity::getCreateDate);

        Page<RecommendEntity> page = new Page<>(curPage, limit);

        page = this.page(page, wrapper);

        return page;
    }

    @Override
    public List<RecommendVO> toListVO(List<RecommendEntity> entities) {
        List<RecommendVO> recommendVOS = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(entities)){
            List<String> infoIds = entities.stream().map(RecommendEntity::getInfoId).toList();
            List<RecInfoEntity> infoEntities = recInfoService.listByIds(infoIds);
            List<RecInfoVO> infoVOS = recInfoService.toVoList(infoEntities);
            Map<String, RecInfoVO> infoVOMap = infoVOS.stream().collect(Collectors.toMap(RecInfoVO::getId, v -> v));

            for(RecommendEntity entity : entities){
                RecommendVO recommendVO = BeanUtil.copyProperties(entity, RecommendVO.class);
                recommendVO.setRecInfoVO(infoVOMap.get(entity.getInfoId()));
                recommendVOS.add(recommendVO);
            }
        }
        return recommendVOS;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pass(String id, String auditIdea) {
        RecommendEntity entity = this.getById(id);
        Assert.notNull(entity, "推荐不存在");
        Assert.isFalse(entity.getAuditStatus().equals(2), "该推荐已通过");
        if(auditIdea!=null){
            entity.setAuditIdea(auditIdea);
        }
        entity.setAuditor(SecurityUser.getUser().getId());
        entity.setAuditDate(new Date());
        entity.setAuditStatus(2);
        String infoId = entity.getInfoId();
        RecInfoEntity infoEntity = recInfoService.getById(infoId);
        Assert.notNull(infoEntity, "菜谱已不存在");

        RecInfoEntity public_Info = BeanUtil.copyProperties(infoEntity,
                RecInfoEntity.class
                ,  "id", "createDate", "updateDate", "creator", "updater", "userId"
        );
        public_Info.setScope(0);
        recInfoService.save(public_Info);
        List<RecProcessEntity> processList = recProcessService.listByInfoId(infoId);
        if(CollectionUtil.isNotEmpty(processList)){
            List<RecProcessEntity> publicProcessList = processList.stream()
                    .map(e -> {
                        RecProcessEntity copy = BeanUtil.copyProperties(e,
                                RecProcessEntity.class
                                ,  "id", "createDate", "updateDate", "creator", "updater", "infoId"
                        );
                        copy.setInfoId(public_Info.getId());
                        return copy;
                    })
                    .toList();

            recProcessService.saveBatch(publicProcessList);
        }

        this.updateById(entity);

    }
}
