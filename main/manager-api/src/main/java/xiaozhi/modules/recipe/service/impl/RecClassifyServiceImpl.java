package xiaozhi.modules.recipe.service.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import xiaozhi.modules.recipe.dao.RecClassifyDao;
import xiaozhi.modules.recipe.entity.RecClassifyEntity;
import xiaozhi.modules.recipe.service.RecClassifyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

@Service
public class RecClassifyServiceImpl extends ServiceImpl<RecClassifyDao, RecClassifyEntity> implements RecClassifyService {

    @Override
    public List<Tree<String>> getTree() {
        LambdaQueryWrapper<RecClassifyEntity> queryWrapper = Wrappers.lambdaQuery(RecClassifyEntity.class);
        /*if(ObjectUtil.isNotEmpty(ids)){
            queryWrapper.eq(RecClassifyEntity::getLevel, 1).or().in(RecClassifyEntity::getId, ids);
        }*/
        List<RecClassifyEntity> list = this.list(queryWrapper);
        //配置
        TreeNodeConfig treeNodeConfig = new TreeNodeConfig();
        // 自定义属性名
        treeNodeConfig.setWeightKey("sort"); // 权重排序字段 默认为weight
        //treeNodeConfig.setIdKey("id"); // 默认为id可以不设置
        //treeNodeConfig.setNameKey("typeName"); // 节点名对应名称 默认为name
        //treeNodeConfig.setParentIdKey(""); // 父节点 默认为parentId
        treeNodeConfig.setChildrenKey("children"); // 子点 默认为children
        //treeNodeConfig.setDeep(3); // 可以配置递归深度 从0开始计算 默认此配置为空,即不限制

        List<Tree<String>> treeNodes = TreeUtil.build(list, "-1", treeNodeConfig,
                // treeNode – 源数据实体
                // tree – 树节点实体
                (treeNode, tree) -> {
                    tree.setId(String.valueOf(treeNode.getId()));
                    tree.setParentId(String.valueOf(treeNode.getParentId()));
                    tree.setWeight(treeNode.getSort());
                    tree.setName(treeNode.getName());
                    // 扩展属性 ...
                    tree.putExtra("level", treeNode.getLevel());
                });
        return treeNodes;
    }

    @Override
    public void delete(List<String> ids) {
        for (String id : ids) {
            recursiveDelete(id);
        }
    }

    private void recursiveDelete(String id) {
        // 删除当前分类
        this.removeById(id);

        // 查找所有子分类
        List<RecClassifyEntity> children = this.list(Wrappers.lambdaQuery(RecClassifyEntity.class)
                .eq(RecClassifyEntity::getParentId, id));

        // 递归删除子分类
        for (RecClassifyEntity child : children) {
            recursiveDelete(child.getId());
        }
    }

}
