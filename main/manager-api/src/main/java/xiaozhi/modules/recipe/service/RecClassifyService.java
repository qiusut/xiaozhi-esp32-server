package xiaozhi.modules.recipe.service;

import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.common.service.BaseService;
import xiaozhi.modules.recipe.entity.RecClassifyEntity;

import java.util.List;

/**
 * 分类管理
 */
public interface RecClassifyService extends IService<RecClassifyEntity> {


    List<Tree<String>> getTree();


    void delete(List<String> ids);
}
