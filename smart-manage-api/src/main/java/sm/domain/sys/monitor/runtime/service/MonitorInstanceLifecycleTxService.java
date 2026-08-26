package sm.domain.sys.monitor.runtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 实例退役是目录生命周期命令，与“故障恢复”语义无关。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class MonitorInstanceLifecycleTxService {
  private final JdbcTemplate jdbcTemplate;

  void retire(String instanceId) {
    int changed =
        jdbcTemplate.update(
            """
            UPDATE t_sys_monitor_instance SET lifecycle='RETIRED',retired_at=now()
            WHERE instance_id=? AND lifecycle='ACTIVE'
            """,
            instanceId);
    if (changed != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "实例不存在或已退役");
    jdbcTemplate.update(
        """
        UPDATE t_sys_monitor_alert_incident SET status='CLOSED',close_reason='INSTANCE_RETIRED',
        last_evaluated_at=now(),version=version+1
                WHERE scope_type='INSTANCE' AND scope_id=? AND status IN ('PENDING','FIRING')
        """,
        instanceId);
  }
}
