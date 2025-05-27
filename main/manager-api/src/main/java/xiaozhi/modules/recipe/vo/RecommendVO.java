package xiaozhi.modules.recipe.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xiaozhi.common.redis.SysCommonCache;
import xiaozhi.common.utils.DateUtils;

import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "菜谱推荐")
public class RecommendVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "菜品id")
    private String infoId;

    @Schema(description = "推荐人Id")
    private Long userId;

    @Schema(description = "推荐人名称")
    private String userName;

    @Schema(description = "审核人")
    private Long auditor;

    @Schema(description = "审核人名称")
    private String auditorName;

    @Schema(description = "审核意见")
    private String auditIdea;

    @Schema(description = "审核状态，0:草稿，1推荐中，2：推荐成功,3:驳回")
    private Integer auditStatus;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date auditDate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建者")
    private Long creator;

    @Schema(description = "更新者")
    private Long updater;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date updateDate;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN,timezone="GMT+8")
    private Date createDate;

    @Schema(description = "菜谱详情")
    private RecInfoVO recInfoVO;


    public String getUserName() {
        if(this.userId != null){
            userName = SysCommonCache.getUserNameWithFallback(String.valueOf(this.userId));
        }
        return userName;
    }

    public String getAuditorName() {
        if(this.auditor != null){
            auditorName = SysCommonCache.getUserNameWithFallback(String.valueOf(this.auditor));
        }
        return auditorName;
    }

}
