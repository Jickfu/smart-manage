package sm.system.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;
import sm.system.util.TraceIdUtil;

/**
 * 统一接口响应体。
 *
 * @author Chekfu
 */
@Schema(description = "统一接口响应")
@Getter
public class Result<T> {
    @Schema(description = "业务响应码，0 表示成功，非零表示失败")
    private final Integer code;
    @Schema(description = "响应说明")
    private final String msg;
    @Schema(description = "响应数据")
    private final T data;
    @Schema(description = "链路追踪ID")
    private final String traceId;
    @Schema(description = "客户端反馈强度，失败时为 WARNING 或 ERROR，成功时为空")
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
