package xiaozhi.modules.recipe.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.recipe.entity.RecommendEntity;

/**
 * 菜谱推荐
 */
@Mapper
public interface RecommendDao extends BaseDao<RecommendEntity> {

}