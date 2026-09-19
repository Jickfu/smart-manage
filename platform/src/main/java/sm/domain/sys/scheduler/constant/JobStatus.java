package sm.domain.sys.scheduler.constant;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Arrays;

/** 定时任务定义状态。 */
public enum JobStatus {
    ENABLED,
    PAUSED;

    public static JobStatus require(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new BizException(ResultEnum.PARAM_ERROR, "任务状态不合法"));
    }
}
