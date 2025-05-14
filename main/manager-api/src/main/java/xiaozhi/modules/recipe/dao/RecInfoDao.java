package xiaozhi.modules.recipe.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.recipe.entity.RecInfoEntity;

/**
 * 菜谱
 */
@Mapper
public interface RecInfoDao extends BaseDao<RecInfoEntity> {

}