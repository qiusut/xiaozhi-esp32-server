package xiaozhi.modules.recipe.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.recipe.entity.RecActionEntity;
import xiaozhi.modules.recipe.entity.RecommendEntity;
import xiaozhi.modules.recipe.vo.RecommendVO;

import java.util.List;
import java.util.Map;

/**
 * 菜谱推荐
 */
public interface RecommendService extends IService<RecommendEntity> {

    public Page<RecommendEntity> getPage(Map<String, Object> params);

    List<RecommendVO> toListVO(List<RecommendEntity> entities);

    public void pass(String id, String auditIdea);

}
