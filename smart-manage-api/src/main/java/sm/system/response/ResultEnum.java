package sm.system.response;

import lombok.Getter;

/**
 * 系统统一业务响应码。
 *
 * @author Chekfu
 */
@Getter
public enum ResultEnum {
    SUCCESS(0, "", null),
    BAD_REQUEST(100400, "错误的请求", FeedbackLevel.WARNING),
    UNAUTHORIZED(100401, "未登录", FeedbackLevel.ERROR),
    PERMISSION_ERROR(100403, "没有权限", FeedbackLevel.ERROR),
    NOT_FOUND(100404, "资源不存在", FeedbackLevel.WARNING),
    DATA_CONFLICT(100409, "数据已被其他请求修改", FeedbackLevel.WARNING),
    UNIQUE_CONFLICT(100410, "数据唯一性冲突", FeedbackLevel.WARNING),
    FOREIGN_KEY_CONFLICT(100411, "数据仍被其他资源引用", FeedbackLevel.WARNING),
    FILE_TOO_LARGE(100413, "上传文件超过大小限制", FeedbackLevel.WARNING),
    PARAM_ERROR(100422, "参数异常", FeedbackLevel.WARNING),
    CSRF_TOKEN_INVALID(100419, "安全校验失败，请刷新页面后重试", FeedbackLevel.ERROR),
    REQUEST_LIMIT(100429, "请求过于频繁，请稍后再试", FeedbackLevel.WARNING),
    SERVER_ERROR(100500, "系统异常，请稍候再试", FeedbackLevel.ERROR),
    SQL_ERROR(100501, "SQL异常，请联系管理员处理", FeedbackLevel.ERROR),
    CONFIG_ERROR(100502, "系统配置异常，请联系管理员处理", FeedbackLevel.ERROR),
    PERSISTENCE_ERROR(100503, "数据持久化失败，请联系管理员处理", FeedbackLevel.ERROR),
    EXTERNAL_SERVICE_ERROR(100504, "外部服务调用失败，请稍后重试", FeedbackLevel.ERROR),
    CAPTCHA_ERROR(101600, "验证码错误", FeedbackLevel.WARNING),
    CAPTCHA_EXPIRE(101601, "验证码已过期", FeedbackLevel.WARNING),
    BILL_STATUS_ERROR(200001, "单据状态不允许当前操作", FeedbackLevel.WARNING),
    ;

    /**
     * 业务响应码。成功固定为 0，错误码不复用 HTTP 状态码。
     */
    private final int code;

    /**
     * 响应消息。
     */
    private final String msg;

    private final FeedbackLevel feedbackLevel;

    ResultEnum(int code, String msg, FeedbackLevel feedbackLevel) {
        this.code = code;
        this.msg = msg;
        this.feedbackLevel = feedbackLevel;
    }

    /** 整数工厂同样从枚举取级别；未知错误码不得被弱化为业务提醒。 */
    static FeedbackLevel feedbackLevelFor(int code) {
        for (ResultEnum result : values()) {
            if (result.code == code) return result.feedbackLevel;
        }
        return FeedbackLevel.ERROR;
    }
}
