package xiaozhi.modules.recipe.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.recipe.entity.RecProcessEntity;

import java.util.List;

/**
 * 菜谱
 */
public interface RecProcessService extends IService<RecProcessEntity> {

    List<RecProcessEntity> listByInfoId(String infoId);

}
