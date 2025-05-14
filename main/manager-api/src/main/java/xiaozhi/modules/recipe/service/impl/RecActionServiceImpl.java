package xiaozhi.modules.recipe.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xiaozhi.common.constant.Constant;
import xiaozhi.modules.recipe.dao.RecActionDao;
import xiaozhi.modules.recipe.entity.RecActionEntity;
import xiaozhi.modules.recipe.service.RecActionService;

import java.util.Map;

@Service
public class RecActionServiceImpl extends ServiceImpl<RecActionDao, RecActionEntity> implements RecActionService {

    public Page<RecActionEntity> getPage(Map<String, Object> params){
        // 分页参数
        long curPage = 1;
        long limit = 10;

        if (params.get(Constant.PAGE) != null) {
            curPage = Long.parseLong((String) params.get(Constant.PAGE));
        }
        if (params.get(Constant.LIMIT) != null) {
            limit = Long.parseLong((String) params.get(Constant.LIMIT));
        }
        QueryWrapper<RecActionEntity> wrapper = new QueryWrapper<>();
        if(params.containsKey("name")){
            wrapper.lambda().like(ObjectUtil.isNotEmpty(params.get("name")),RecActionEntity::getName, params.get("name"));
        }
        wrapper.lambda().orderByDesc(RecActionEntity::getCreateDate);

        Page<RecActionEntity> page = new Page<>(curPage, limit);

        page = this.page(page, wrapper);

        return page;
    }

}
