package xiaozhi.modules.recipe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.recipe.entity.RecActionEntity;

import java.util.Map;

/**
 * 操作
 */
public interface RecActionService extends IService<RecActionEntity> {

    public Page<RecActionEntity> getPage(Map<String, Object> params);

}
