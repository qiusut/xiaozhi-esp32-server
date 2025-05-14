package xiaozhi.modules.recipe.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.recipe.entity.RecProcessEntity;

/**
 * 菜谱
 */
@Mapper
public interface RecProcessDao extends BaseDao<RecProcessEntity> {

}