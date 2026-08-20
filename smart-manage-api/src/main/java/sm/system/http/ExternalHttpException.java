package sm.system.http;

import lombok.Getter;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 出站 HTTP 调用失败，异常信息不得包含请求头、Cookie、凭据或完整响应报文。 */
@Getter
public class ExternalHttpException extends BizException {
    private final FailureType failureType;
    private final Integer statusCode;

    public ExternalHttpException(FailureType failureType, String detail) {
        this(failureType, null, detail, null);
    }

    public ExternalHttpException(FailureType failureType, Integer statusCode, String detail) {
        this(failureType, statusCode, detail, null);
    }

    public ExternalHttpException(FailureType failureType, String detail, Throwable cause) {
        this(failureType, null, detail, cause);
    }

    private ExternalHttpException(FailureType failureType, Integer statusCode, String detail, Throwable cause) {
        super(ResultEnum.EXTERNAL_SERVICE_ERROR, detail);
        this.failureType = failureType;
        this.statusCode = statusCode;
        if (cause != null) {
            initCause(cause);
        }
    }

    public enum FailureType {
        REQUEST_BUILD,
        CONNECTION,
        TIMEOUT,
        INTERRUPTED,
        HTTP_STATUS,
        RESPONSE_PARSE
    }
}
