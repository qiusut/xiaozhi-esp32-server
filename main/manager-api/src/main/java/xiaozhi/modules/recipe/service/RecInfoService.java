package xiaozhi.modules.recipe.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;
import xiaozhi.modules.recipe.dto.RecInfoDTO;
import xiaozhi.modules.recipe.entity.RecInfoEntity;
import xiaozhi.modules.recipe.vo.RecInfoVO;

import java.util.List;
import java.util.Map;

/**
 * 菜谱
 */
public interface RecInfoService extends IService<RecInfoEntity> {


    public void add(RecInfoDTO dto);

    void edit(RecInfoVO vo);

    void delete(String id);

    public Page<RecInfoEntity> getPage(Map<String, Object> params);


    List<RecInfoVO> toVoList(List<RecInfoEntity> list);

    String sendRecipe(String device_mac, String id);

    JSONObject getUserRecipe(String device_mac);

    void initRedis();
}
