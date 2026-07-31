package sm.domain.sys.scheduler.constant;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Arrays;

/** 定时任务执行实例状态。 */
public enum JobExecutionStatus {
    RUNNING,
    SUCCESS,
    FAILED,
    SKIPPED;

    public static JobExecutionStatus require(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultEnum.PARAM_ERROR, "执行状态不合法"));
    }
}
