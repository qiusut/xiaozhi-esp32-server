package xiaozhi.modules.bind.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xiaozhi.common.redis.SysCommonCache;

import java.util.Date;

@Data
@Schema(description = "设备共享表")
public class DeviceShareVo {

    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(description = "ID")
    private String id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "设备类型，ai设备，tb设备")
    private String type;

    @Schema(description = "共享的设备id")
    private String deviceId;

    @Schema(description = "关联用户登入名")
    private String username;
    public String getUsername(){
        return SysCommonCache.getUserValueById(userId,"username");
    }

    @Schema(description = "关联用户姓名")
    private String realName;
    public String getRealName(){
        return SysCommonCache.getUserValueById(userId,"realName");
    }

    @Schema(description = "共享用户手机号")
    private String mobile;
    public String getMobile(){
        return SysCommonCache.getUserValueById(userId,"mobile");
    }

    @Schema(description = "创建时间")
    private Date createDate;

}
