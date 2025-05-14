package xiaozhi.modules.recipe.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xiaozhi.modules.recipe.dao.RecProcessDao;
import xiaozhi.modules.recipe.entity.RecProcessEntity;
import xiaozhi.modules.recipe.service.RecProcessService;

import java.util.List;

@Service
public class RecProcessServiceImpl extends ServiceImpl<RecProcessDao, RecProcessEntity> implements RecProcessService {

    @Override
    public List<RecProcessEntity> listByInfoId(String infoId){
        return this.list(Wrappers.lambdaQuery(RecProcessEntity.class).eq(RecProcessEntity::getInfoId, infoId));
    }

}
