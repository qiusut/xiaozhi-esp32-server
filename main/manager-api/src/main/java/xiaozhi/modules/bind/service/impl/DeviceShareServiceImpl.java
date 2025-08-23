package xiaozhi.modules.bind.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xiaozhi.common.user.UserDetail;
import xiaozhi.modules.agent.dao.AgentDao;
import xiaozhi.modules.agent.entity.AgentEntity;
import xiaozhi.modules.bind.dao.DeviceShareDao;
import xiaozhi.modules.bind.enums.DeviceTypeEnum;
import xiaozhi.modules.bind.model.dto.DeviceShareDto;
import xiaozhi.modules.bind.model.entity.DeviceShareEntity;
import xiaozhi.modules.bind.model.vo.DeviceShareVo;
import xiaozhi.modules.bind.service.DeviceShareService;
import xiaozhi.modules.device.dao.DeviceDao;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.security.user.SecurityUser;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.service.SysParamsService;
import xiaozhi.modules.sys.service.SysUserPlusService;
import xiaozhi.modules.tb.dao.TbDeviceDao;
import xiaozhi.modules.tb.entity.TbDeviceEntity;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceShareServiceImpl extends ServiceImpl<DeviceShareDao, DeviceShareEntity> implements DeviceShareService {

    @Resource
    private SysUserPlusService sysUserPlusService;
    @Resource
    private SysParamsService sysParamsService;
    @Resource
    private TbDeviceDao tbDeviceDao;
    @Resource
    private DeviceDao deviceDao;
    @Resource
    private AgentDao agentDao;

    @Override
    public List<DeviceShareVo> getList(String deviceId) {
        List<DeviceShareVo> deviceShareVos = new ArrayList<>();
        List<DeviceShareEntity> list = this.list(Wrappers.lambdaQuery(DeviceShareEntity.class).eq(DeviceShareEntity::getDeviceId, deviceId));
        if(CollUtil.isNotEmpty(list)){
            deviceShareVos = BeanUtil.copyToList(list, DeviceShareVo.class);
        }
        return deviceShareVos;
    }


    @Override
    public void shareDevice(DeviceShareDto deviceShareDto) {
        String mobile = deviceShareDto.getMobile();
        String deviceId = deviceShareDto.getDeviceId();
        String type = deviceShareDto.getType();
        DeviceShareEntity deviceShareEntity = new DeviceShareEntity();
        UserDetail userDetail = SecurityUser.getUser();
        SysUserEntity sysUserEntity = sysUserPlusService.getOne(Wrappers.lambdaQuery(SysUserEntity.class).eq(SysUserEntity::getMobile, mobile));
        Assert.notNull(sysUserEntity,"手机号不存在");
        if(StrUtil.equals(type,DeviceTypeEnum.AI.getCode())||deviceId.contains(":")){
            DeviceEntity deviceEntity = deviceDao.selectById(deviceId);
            if(deviceEntity!=null&&deviceEntity.getUserId().equals(userDetail.getId())){
                deviceShareEntity.setDeviceId(deviceId);
                deviceShareEntity.setUserId(sysUserEntity.getId());
                deviceShareEntity.setType(DeviceTypeEnum.AI.getCode());
            }
        }else {
            TbDeviceEntity tbDeviceEntity = tbDeviceDao.selectById(deviceId);
            if(tbDeviceEntity!=null&&tbDeviceEntity.getUserId().equals(userDetail.getId())){
                deviceShareEntity.setDeviceId(deviceId);
                deviceShareEntity.setUserId(sysUserEntity.getId());
                deviceShareEntity.setType(DeviceTypeEnum.TB.getCode());
            }
        }
        Assert.isTrue(StrUtil.isNotBlank(deviceShareEntity.getDeviceId()),"设备不存在");
        List<DeviceShareEntity> list = this.list(Wrappers.lambdaQuery(DeviceShareEntity.class).eq(DeviceShareEntity::getDeviceId, deviceShareEntity.getDeviceId()));
        if(CollUtil.isNotEmpty(list)){
            Assert.isFalse(list.stream().anyMatch(e -> e.getUserId().equals(sysUserEntity.getId())),"设备已分享给该用户");
            String device_share_size = sysParamsService.getValue("device_share_size", true);
            Assert.isTrue(list.size()>=Integer.parseInt(device_share_size),"设备分享超出最大限制");
        }
        this.save(deviceShareEntity);
    }

    @Override
    public List<JSONObject> getAgentList() {
        List<JSONObject> agentJsonList = new ArrayList<>();
        Long userId = SecurityUser.getUserId();
        List<String> agentIds = new ArrayList<>();
        List<DeviceShareEntity> list = this.list(Wrappers.lambdaQuery(DeviceShareEntity.class).eq(DeviceShareEntity::getUserId, userId));
        if(CollUtil.isNotEmpty(list)){
            List<String> deviceIds = list.stream().map(DeviceShareEntity::getDeviceId).toList();
            List<String> agentEntities = deviceDao.selectObjs(Wrappers.lambdaQuery(DeviceEntity.class).select(DeviceEntity::getAgentId).in(DeviceEntity::getId, deviceIds));
            if(CollUtil.isNotEmpty(agentEntities)){
                agentIds = agentEntities.stream().distinct().toList();
            }
        }

        List<AgentEntity> agentList = agentDao.selectList(Wrappers.lambdaQuery(AgentEntity.class).eq(AgentEntity::getUserId, userId).or().in(CollUtil.isNotEmpty(agentIds),AgentEntity::getId, agentIds));
        if(CollUtil.isNotEmpty(agentList)){
            agentList.sort((a, b) -> {
                boolean aIsOwn = a.getUserId().equals(userId);
                boolean bIsOwn = b.getUserId().equals(userId);

                if (aIsOwn && !bIsOwn) return -1;
                if (!aIsOwn && bIsOwn) return 1;
                return 0;
            });
            for(AgentEntity agentEntity:agentList){
                JSONObject agentJson = new JSONObject();
                agentJson.set("id", agentEntity.getId());
                agentJson.set("agentName", agentEntity.getAgentName());
                agentJson.set("isShare",agentIds.contains(agentEntity.getId()));

                agentJsonList.add(agentJson);
            }
        }

        return agentJsonList;
    }

}