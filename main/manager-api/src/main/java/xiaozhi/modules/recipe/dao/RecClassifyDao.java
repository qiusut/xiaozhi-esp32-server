package xiaozhi.modules.recipe.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.recipe.entity.RecClassifyEntity;

/**
 * 菜谱分类
 */
@Mapper
public interface RecClassifyDao extends BaseDao<RecClassifyEntity> {

}