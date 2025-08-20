package xiaozhi.modules.sys.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.device.dao.DeviceDao;
import xiaozhi.modules.device.entity.DeviceEntity;
import xiaozhi.modules.sys.dao.SysUserDao;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.query.SysUserQuery;
import xiaozhi.modules.sys.service.SysUserPlusService;
import xiaozhi.modules.sys.vo.SysUserVO;
import xiaozhi.modules.tb.dao.TbDeviceDao;
import xiaozhi.modules.tb.entity.TbDeviceEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysUserPlusServiceImpl extends ServiceImpl<SysUserDao, SysUserEntity> implements SysUserPlusService {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private TbDeviceDao tbDeviceDao;

    @Override
    public PageData<SysUserVO> page(SysUserQuery query) {
        Page<SysUserEntity> page = Page.of(query.getPage(), query.getLimit());
        page = this.page(page, Wrappers.lambdaQuery(SysUserEntity.class)
                .eq(StrUtil.isNotBlank(query.getUsername()), SysUserEntity::getUsername, query.getUsername())
        );
        List<SysUserVO> sysUserVOList = new ArrayList<>();
        if(CollUtil.isNotEmpty(page.getRecords())){
            List<Long> userIds = page.getRecords().stream().map(SysUserEntity::getId).toList();
            Map<Long, Integer> deviceCountMap = new HashMap<>();
            Map<Long, Integer> tbDeviceCountMap = new HashMap<>();
            List<DeviceEntity> deviceEntityList = deviceDao.selectList(Wrappers.lambdaQuery(DeviceEntity.class).in(DeviceEntity::getUserId, userIds));
            if(CollUtil.isNotEmpty(deviceEntityList)){
                deviceCountMap = deviceEntityList.stream()
                        .collect(Collectors.toMap(
                                DeviceEntity::getUserId,
                                e -> 1,
                                Integer::sum
                        ));

            }
            List<TbDeviceEntity> tbDeviceEntityList = tbDeviceDao.selectList(Wrappers.lambdaQuery(TbDeviceEntity.class).in(TbDeviceEntity::getUserId, userIds));
            if(CollUtil.isNotEmpty(tbDeviceEntityList)){
                tbDeviceCountMap = deviceEntityList.stream()
                        .collect(Collectors.toMap(
                                DeviceEntity::getUserId,
                                e -> 1,
                                Integer::sum
                        ));

            }
            for (SysUserEntity sysUserEntity : page.getRecords()) {
                SysUserVO sysUserVO = new SysUserVO();
                BeanUtil.copyProperties(sysUserEntity, sysUserVO);
                sysUserVO.setDeviceCount(deviceCountMap.getOrDefault(sysUserEntity.getId(), 0));
                sysUserVO.setTbDeviceCount(tbDeviceCountMap.getOrDefault(sysUserEntity.getId(), 0));
                sysUserVOList.add(sysUserVO);
            }
        }


        return new PageData<>(sysUserVOList, page.getTotal());
    }
}
