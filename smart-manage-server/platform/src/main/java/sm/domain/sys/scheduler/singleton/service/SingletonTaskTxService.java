package sm.domain.sys.scheduler.singleton.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
/** 管理端期望状态与事件在同一事务提交。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class SingletonTaskTxService {
    private final SingletonTaskAccessor accessor;
    public void control(String number, int version, boolean requested) {
        if (accessor.control(number, version, requested) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "任务已变更或不允许控制，请刷新后重试");
        }
    }
}
