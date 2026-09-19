package sm.system.response;

import lombok.Getter;
import sm.system.util.TraceIdUtil;

/**
 * 统一接口响应体。
 *
 * @author Chekfu
 */
@Getter
public class Result<T> {
    private final Integer code;
    private final String msg;
    private final T data;
    private final String traceId;
    private final FeedbackLevel feedbackLevel;

    private Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = java.util.Objects.requireNonNull(msg, "响应说明不能为 null");
        this.data = data;
        this.traceId = TraceIdUtil.getTraceId();
        this.feedbackLevel = ResultEnum.feedbackLevelFor(code);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultEnum.SUCCESS.getCode(), "", data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        if (code == null || code == ResultEnum.SUCCESS.getCode()) {
            throw new IllegalArgumentException("失败响应必须使用非零业务码");
        }
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return error(ResultEnum.SERVER_ERROR.getCode(), message);
    }

    public static <T> Result<T> error(ResultEnum resultEnum) {
        return error(resultEnum.getCode(), resultEnum.getMsg());
    }

    public static <T> Result<T> error(ResultEnum resultEnum, String errorMessage) {
        return error(resultEnum.getCode(), resultEnum.getMsg() + "：" + errorMessage);
    }
}
