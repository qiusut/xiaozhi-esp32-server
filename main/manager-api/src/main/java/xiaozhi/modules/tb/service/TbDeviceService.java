package xiaozhi.modules.tb.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.tb.dto.TbDeviceRpcDTO;
import xiaozhi.modules.tb.dto.TbFunctionDTO;
import xiaozhi.modules.tb.entity.TbFunctionEntity;
import xiaozhi.modules.tb.query.DeviceInfoQuery;
import xiaozhi.modules.tb.vo.TbFunctionVO;

import java.util.List;

public interface TbDeviceService extends IService<TbFunctionEntity> {

    List<JSONObject> infoList(DeviceInfoQuery query);

    public Page<TbFunctionEntity> deviceTypeList(Integer curPage, Integer limit, String type, String name);

    public void addFunction(TbFunctionDTO tbFunctionDTO);

    public void updateFunction(TbFunctionVO tbFunctionVO);

    List<String> initTbToken(String agentId);

    String sendDeviceRpc(TbDeviceRpcDTO tbDeviceRpc);

    void initRedis();

    //public void deleteByType(String type);

}