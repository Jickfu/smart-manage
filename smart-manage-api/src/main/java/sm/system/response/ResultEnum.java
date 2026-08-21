package sm.system.response;

import lombok.Getter;

/**
 * 系统统一业务响应码。
 *
 * @author Chekfu
 */
@Getter
public enum ResultEnum {
    SUCCESS(0, ""),
    BAD_REQUEST(100400, "错误的请求"),
    UNAUTHORIZED(100401, "未登录"),
    PERMISSION_ERROR(100403, "没有权限"),
    NOT_FOUND(100404, "资源不存在"),
    DATA_CONFLICT(100409, "数据已被其他请求修改"),
    UNIQUE_CONFLICT(100410, "数据唯一性冲突"),
    FOREIGN_KEY_CONFLICT(100411, "数据仍被其他资源引用"),
    FILE_TOO_LARGE(100413, "上传文件超过大小限制"),
    PARAM_ERROR(100422, "参数异常"),
    CSRF_TOKEN_INVALID(100419, "安全校验失败，请刷新页面后重试"),
    REQUEST_LIMIT(100429, "请求过于频繁，请稍后再试"),
    SERVER_ERROR(100500, "系统异常，请稍候再试"),
    SQL_ERROR(100501, "SQL异常，请联系管理员处理"),
    CONFIG_ERROR(100502, "系统配置异常，请联系管理员处理"),
    PERSISTENCE_ERROR(100503, "数据持久化失败，请联系管理员处理"),
    EXTERNAL_SERVICE_ERROR(100504, "外部服务调用失败，请稍后重试"),
    CAPTCHA_ERROR(101600, "验证码错误"),
    CAPTCHA_EXPIRE(101601, "验证码已过期"),
    BILL_STATUS_ERROR(200001, "单据状态不允许当前操作"),
    ;

    /**
     * 业务响应码。成功固定为 0，错误码不复用 HTTP 状态码。
     */
    private final int code;

    /**
     * 响应消息。
     */
    private final String msg;

    ResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
