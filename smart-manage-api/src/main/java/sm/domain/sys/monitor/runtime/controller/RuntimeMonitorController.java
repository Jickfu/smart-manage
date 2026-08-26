package sm.domain.sys.monitor.runtime.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.domain.sys.monitor.runtime.constant.RuntimeMonitorPermission;
import sm.domain.sys.monitor.runtime.model.vo.*;
import sm.domain.sys.monitor.runtime.service.*;
import sm.system.response.Result;

@RestController
@RequestMapping("/sys/monitor/runtime")
@RequiredArgsConstructor
public class RuntimeMonitorController {
  private final MonitorTopologyService topologyService;
  private final MonitorHistoryService historyService;
  private final MonitorSnapshotService snapshotService;

  @GetMapping("/instances")
  @SaCheckPermission(RuntimeMonitorPermission.VIEW)
  public Result<List<MonitorInstanceVO>> instances() {
    return Result.success(topologyService.onlineInstances());
  }

  @GetMapping("/topology")
  @SaCheckPermission(RuntimeMonitorPermission.VIEW)
  public Result<List<MonitorTopologyVO>> topology() {
    return Result.success(topologyService.topology());
  }

  @GetMapping("/host-snapshot")
  @SaCheckPermission(RuntimeMonitorPermission.VIEW)
  public Result<HostSnapshotVO> hostSnapshot(@RequestParam String hostId) {
    return Result.success(snapshotService.host(hostId));
  }

  @GetMapping("/instance-snapshot")
  @SaCheckPermission(RuntimeMonitorPermission.VIEW)
  public Result<InstanceSnapshotVO> instanceSnapshot(
      @RequestParam(required = false) String instanceId) {
    return Result.success(snapshotService.instance(instanceId));
  }

  @GetMapping("/history")
  @SaCheckPermission(RuntimeMonitorPermission.VIEW)
  public Result<List<MonitorHistoryPointVO>> history(
      @RequestParam String scopeType,
      @RequestParam String scopeId,
      @RequestParam(defaultValue = "1h") String range) {
    return Result.success(historyService.history(scopeType, scopeId, range));
  }

  @PostMapping("/instances/retire")
  @SaCheckPermission(RuntimeMonitorPermission.MANAGE)
  public Result<Void> retire(@RequestParam String instanceId) {
    topologyService.retire(instanceId);
    return Result.success();
  }
}
