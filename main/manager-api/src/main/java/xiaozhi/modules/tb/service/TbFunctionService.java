package xiaozhi.modules.tb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.tb.dto.TbFunctionDTO;
import xiaozhi.modules.tb.entity.TbFunctionEntity;
import xiaozhi.modules.tb.vo.TbFunctionVO;

public interface TbFunctionService extends IService<TbFunctionEntity> {

    public Page<TbFunctionEntity> deviceTypeList(Integer curPage, Integer limit, String type, String name);

    public void addFunction(TbFunctionDTO tbFunctionDTO);

    public void updateFunction(TbFunctionVO tbFunctionVO);

    void initFunctionRedis();
}