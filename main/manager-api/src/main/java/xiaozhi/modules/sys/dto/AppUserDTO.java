package xiaozhi.modules.sys.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * APP用户修改
 */
@Data
@Schema(description = "APP用户修改")
public class AppUserDTO implements Serializable {

    @Schema(description = "用户名")
    private String username;


    @Schema(description = "姓名")
    private String realName;

    @Schema(description = "头像")
    private String headUrl;

    @Schema(description = "性别   0：男   1：女    2：保密")
    private Integer gender;

}