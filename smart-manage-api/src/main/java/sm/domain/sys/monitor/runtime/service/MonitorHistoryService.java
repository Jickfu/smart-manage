package sm.domain.sys.monitor.runtime.service;

import java.sql.ResultSet;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.runtime.model.vo.MonitorHistoryPointVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Service
@RequiredArgsConstructor
public class MonitorHistoryService {
  private final JdbcTemplate jdbcTemplate;

  public List<MonitorHistoryPointVO> history(String scopeType, String scopeId, String range) {
    Range resolved = Range.parse(range);
    if (scopeId == null || scopeId.isBlank())
      throw new BizException(ResultEnum.PARAM_ERROR, "监控对象不能为空");
    if ("HOST".equals(scopeType))
      return jdbcTemplate.query(
          "SELECT date_bin(INTERVAL '"
              + resolved.bucket
              + "',sample_time,TIMESTAMPTZ '2000-01-01') sample_time,avg(cpu_usage)"
              + " cpu_usage,avg(memory_used::numeric/nullif(memory_total,0))"
              + " memory_usage,max(worst_filesystem_usage) filesystem_usage, (array_agg(worst_mount"
              + " ORDER BY worst_filesystem_usage DESC NULLS LAST,sample_time DESC))[1]"
              + " worst_mount, avg(disk_read_rate) disk_read_rate,avg(disk_write_rate)"
              + " disk_write_rate,avg(network_receive_rate)"
              + " network_receive_rate,avg(network_transmit_rate) network_transmit_rate FROM"
              + " t_sys_monitor_host_history WHERE host_id=? AND sample_time>=now()-INTERVAL '"
              + resolved.lookback
              + "' GROUP BY 1 ORDER BY 1",
          (rs, row) -> host(rs),
          scopeId.trim());
    if ("INSTANCE".equals(scopeType))
      return jdbcTemplate.query(
          "SELECT date_bin(INTERVAL '"
              + resolved.bucket
              + "',sample_time,TIMESTAMPTZ '2000-01-01') sample_time,avg(process_cpu)"
              + " process_cpu,avg(heap_used::numeric/nullif(heap_max,0))"
              + " heap_usage,avg(http_request_rate) http_request_rate,avg(http_5xx_rate)"
              + " http_5xx_rate,max(http_p95_ms) http_p95_ms,max(http_p99_ms)"
              + " http_p99_ms,avg(thread_count) thread_count,avg(blocked_thread_count)"
              + " blocked_thread_count,avg(db_active::numeric/nullif(db_max,0))"
              + " db_pool_usage,avg(db_waiting) db_waiting FROM t_sys_monitor_instance_history"
              + " WHERE instance_id=? AND sample_time>=now()-INTERVAL '"
              + resolved.lookback
              + "' GROUP BY 1 ORDER BY 1",
          (rs, row) -> instance(rs),
          scopeId.trim());
    throw new BizException(ResultEnum.PARAM_ERROR, "监控对象类型不合法");
  }

  private MonitorHistoryPointVO host(ResultSet rs) throws java.sql.SQLException {
    var value = base(rs);
    value.setCpuUsage(number(rs, "cpu_usage"));
    value.setMemoryUsage(number(rs, "memory_usage"));
    value.setFilesystemUsage(number(rs, "filesystem_usage"));
    value.setWorstMount(rs.getString("worst_mount"));
    value.setDiskReadBytesPerSecond(number(rs, "disk_read_rate"));
    value.setDiskWriteBytesPerSecond(number(rs, "disk_write_rate"));
    value.setNetworkReceiveBytesPerSecond(number(rs, "network_receive_rate"));
    value.setNetworkTransmitBytesPerSecond(number(rs, "network_transmit_rate"));
    return value;
  }

  private MonitorHistoryPointVO instance(ResultSet rs) throws java.sql.SQLException {
    var value = base(rs);
    value.setProcessCpuUsage(number(rs, "process_cpu"));
    value.setHeapUsage(number(rs, "heap_usage"));
    value.setRequestRate(number(rs, "http_request_rate"));
    value.setServerErrorRate(number(rs, "http_5xx_rate"));
    value.setP95Ms(number(rs, "http_p95_ms"));
    value.setP99Ms(number(rs, "http_p99_ms"));
    value.setThreadCount(number(rs, "thread_count"));
    value.setBlockedThreadCount(number(rs, "blocked_thread_count"));
    value.setDbPoolUsage(number(rs, "db_pool_usage"));
    value.setDbWaiting(number(rs, "db_waiting"));
    return value;
  }

  private MonitorHistoryPointVO base(ResultSet rs) throws java.sql.SQLException {
    var value = new MonitorHistoryPointVO();
    value.setSampleTime(rs.getObject("sample_time", java.time.OffsetDateTime.class));
    return value;
  }

  private Double number(ResultSet rs, String name) throws java.sql.SQLException {
    Number value = (Number) rs.getObject(name);
    return value == null ? null : value.doubleValue();
  }

  enum Range {
    H1("1 hour", "1 minute"),
    H6("6 hours", "5 minutes"),
    H24("24 hours", "10 minutes"),
    D7("7 days", "30 minutes");
    final String lookback, bucket;

    Range(String lookback, String bucket) {
      this.lookback = lookback;
      this.bucket = bucket;
    }

    static Range parse(String value) {
      return switch (value == null ? "1h" : value.toLowerCase(Locale.ROOT)) {
        case "1h" -> H1;
        case "6h" -> H6;
        case "24h" -> H24;
        case "7d" -> D7;
        default -> throw new BizException(ResultEnum.PARAM_ERROR, "历史范围仅支持 1h、6h、24h、7d");
      };
    }
  }
}
