package xiaozhi.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xiaozhi.common.page.PageData;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.query.SysUserQuery;
import xiaozhi.modules.sys.vo.SysUserVO;

/**
 * mybatisplus系统用户
 */
public interface SysUserPlusService extends IService<SysUserEntity> {

    PageData<SysUserVO> page(SysUserQuery query);
}
