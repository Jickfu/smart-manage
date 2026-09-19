package sm.system.exception;

import lombok.Getter;
import sm.system.response.ResultEnum;

/**
 * 可预期的业务异常，必须显式指定统一错误类别。
 *
 * @author Chekfu
 */
@Getter
public class BizException extends RuntimeException {
    /**
     * 异常编号
     */
    private final int code;

    /**
     * 异常信息
     */
    private final String msg;

    public BizException(ResultEnum resultEnum) {
        this(resultEnum, null);
    }

    /**
     * @param resultEnum 统一错误类别
     * @param detail 业务场景补充说明，可为空
     */
    public BizException(ResultEnum resultEnum, String detail) {
        super(buildMessage(resultEnum, detail));
        this.code = resultEnum.getCode();
        this.msg = buildMessage(resultEnum, detail);
    }

    /** 保留基础设施或外部系统异常链，便于启动失败和后台任务日志定位真实根因。 */
    public BizException(ResultEnum resultEnum, String detail, Throwable cause) {
        super(buildMessage(resultEnum, detail), cause);
        this.code = resultEnum.getCode();
        this.msg = buildMessage(resultEnum, detail);
    }

    private static String buildMessage(ResultEnum resultEnum, String detail) {
        if (detail == null || detail.isBlank()) {
            return resultEnum.getMsg();
        }
        return resultEnum.getMsg() + "：" + detail;
    }
}
