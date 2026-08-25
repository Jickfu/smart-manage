package sm.domain.sys.monitor.runtime.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import sm.domain.sys.monitor.common.model.vo.MonitorInstanceVO;
import sm.domain.sys.monitor.runtime.constant.RuntimeMonitorPermission;
import sm.domain.sys.monitor.runtime.model.vo.RuntimeSnapshotVO;
import sm.domain.sys.monitor.runtime.service.RuntimeMonitorService;
import sm.system.response.Result;

import java.util.List;

/**
 * 内建运行监控接口。
 */
@RestController
@Tag(name = "运维中心-运行监控", description = "主机、应用实例、实时快照与历史趋势")
@RequiredArgsConstructor
public class RuntimeMonitorController {

    private final RuntimeMonitorService service;

    @GetMapping("/sys/monitor/runtime/instances")
    @Operation(summary = "在线实例", description = "查询 Redis 注册表中的在线应用实例")
    @SaCheckPermission(RuntimeMonitorPermission.VIEW)
    public Result<List<MonitorInstanceVO>> instances() {
        return Result.success(service.instances());
    }

    @GetMapping("/sys/monitor/runtime/snapshot")
    @Operation(summary = "节点信息", description = "获取指定在线实例 JVM/OS/CPU/内存/磁盘/线程/GC 聚合信息")
    @SaCheckPermission(RuntimeMonitorPermission.VIEW)
    public Result<RuntimeSnapshotVO> snapshot(@RequestParam(required = false) String instanceId) {
        return Result.success(service.snapshot(instanceId));
    }

    @GetMapping("/sys/monitor/runtime/topology")
    @SaCheckPermission(RuntimeMonitorPermission.VIEW)
    public Result<java.util.List<java.util.Map<String,Object>>> topology() { return Result.success(service.topology()); }

    @GetMapping("/sys/monitor/runtime/history")
    @SaCheckPermission(RuntimeMonitorPermission.VIEW)
    public Result<java.util.List<java.util.Map<String,Object>>> history(@RequestParam String scopeType,
            @RequestParam String scopeId, @RequestParam(defaultValue = "1h") String range) {
        return Result.success(service.history(scopeType, scopeId, range));
    }

}
