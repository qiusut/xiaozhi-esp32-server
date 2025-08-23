package xiaozhi.modules.bind.dao;

import org.apache.ibatis.annotations.Mapper;
import xiaozhi.common.dao.BaseDao;
import xiaozhi.modules.bind.model.entity.DeviceShareEntity;
import xiaozhi.modules.tb.entity.TbDeviceEntity;

/**
 * 设备共享
 */
@Mapper
public interface DeviceShareDao extends BaseDao<DeviceShareEntity> {

}