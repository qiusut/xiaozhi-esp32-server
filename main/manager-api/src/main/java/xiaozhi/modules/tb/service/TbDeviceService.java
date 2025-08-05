package xiaozhi.modules.tb.service;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.modules.tb.dto.TbDeviceDTO;
import xiaozhi.modules.tb.dto.TbDeviceRpcDTO;
import xiaozhi.modules.tb.entity.TbDeviceEntity;
import xiaozhi.modules.tb.query.DeviceQueryPage;
import xiaozhi.modules.tb.vo.TbDeviceVO;

import java.util.List;

public interface TbDeviceService extends IService<TbDeviceEntity> {

    TbDeviceVO getTbDevice(String id);

    JSONObject getTbDeviceJson(String tbDeviceId);

    List<JSONObject> getTbDeviceList();

    Page<TbDeviceEntity> getPage(DeviceQueryPage deviceQueryPage);

    void addTbDevice(TbDeviceDTO tbDeviceDTO);

    void deleteTbDevice(String id);

    String initTbToken(boolean isRefresh);

    String sendDeviceRpc(TbDeviceRpcDTO tbDeviceRpc);

    void initTbDeviceRedis(Long userId);

    void initRedis();
}