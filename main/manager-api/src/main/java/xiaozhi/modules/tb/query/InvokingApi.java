package xiaozhi.modules.tb.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Api接口调用")
public class InvokingApi {

    @Schema(description = "租户Id")
    private Long tenantId;

    @Schema(description = "请求方式",required = true, example = "GET,POST,PUT,DELETE", nullable = true)
    private String method;

    @Schema(description = "接口地址",required = true)
    private String url;

    @Schema(description = "请求体")
    private String body;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "请求头")
    private Headers headers;

    @Data
    public static class Headers {
        @Schema(description = "请求体的媒体类型")
        private String contentType;

        @Schema(description = "客户端能够接受的响应内容类型，这里包括application/json（JSON格式）、text/plain（纯文本）以及任何类型（*/*）")
        private String accept;

        @Schema(description = "客户端支持的内容编码类型，如gzip、deflate和br（Brotli压缩）")
        private String AcceptEncoding;

        @Schema(description = "客户端的首选语言，这里指定了中文简体（zh-CN）和中文（zh）的优先级。")
        private String AcceptLanguage;

        @Schema(description = "请求认证token")
        private String XAuthorization;

        @Schema(description = "请求认证token")
        private String Authorization;

    }


}
