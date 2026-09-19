package sm.domain.sys.monitor.common.util;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.LocalDateTime;

/** 日志查询公共参数校验。 */
public final class LogQueryValidator {
    private LogQueryValidator() {
    }

    public static void validateTimeRange(LocalDateTime beginTime, LocalDateTime endTime) {
        if (beginTime != null && endTime != null && beginTime.isAfter(endTime)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "开始时间不能晚于结束时间");
        }
    }

    /** 本人日志等受限入口只能收紧时间范围，不能被客户端传入的更早时间放宽。 */
    public static LocalDateTime resolveRestrictedBeginTime(
            LocalDateTime requestedBeginTime, LocalDateTime restrictedBeginTime) {
        if (restrictedBeginTime == null) return requestedBeginTime;
        if (requestedBeginTime == null || requestedBeginTime.isBefore(restrictedBeginTime)) {
            return restrictedBeginTime;
        }
        return requestedBeginTime;
    }
}
