package sm.domain.sys.monitor.slowsql.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.slowsql.constant.SlowSqlPermission;
import sm.domain.sys.monitor.slowsql.model.form.SlowSqlCommandForm;
import sm.domain.sys.monitor.slowsql.model.form.SlowSqlTargetForm;
import sm.domain.sys.monitor.slowsql.model.vo.SlowSqlSnapshotVO;
import sm.domain.sys.monitor.slowsql.service.SlowSqlService;
import sm.system.response.Result;

@RestController
@Tag(name = "系统监控-慢 SQL", description = "指定实例的 Druid SQL 内存聚合统计")
@RequiredArgsConstructor
public class SlowSqlController {
    private final SlowSqlService service;

    @GetMapping("/sys/monitor/slow-sql/snapshot")
    @Operation(summary = "慢 SQL 统计快照")
    @SaCheckPermission(SlowSqlPermission.ACCESS)
    public Result<SlowSqlSnapshotVO> snapshot(@RequestParam(required = false) String instanceId) {
        return Result.success(service.snapshot(instanceId));
    }

    @PostMapping("/sys/monitor/slow-sql/threshold")
    @Operation(summary = "调整目标实例慢 SQL 阈值")
    @SaCheckPermission(SlowSqlPermission.CONFIG)
    public Result<SlowSqlSnapshotVO> updateThreshold(@RequestBody @Valid SlowSqlCommandForm form) {
        return Result.success(service.updateThreshold(form));
    }

    @PostMapping("/sys/monitor/slow-sql/clear")
    @Operation(summary = "清空目标实例 SQL 内存统计")
    @SaCheckPermission(SlowSqlPermission.CLEAR)
    public Result<SlowSqlSnapshotVO> clear(@RequestBody @Valid SlowSqlTargetForm form) {
        return Result.success(service.clear(form));
    }

    @GetMapping("/sys/monitor/internal/slow-sql/snapshot")
    @SaCheckPermission(SlowSqlPermission.ACCESS)
    public Result<SlowSqlSnapshotVO> localSnapshot() {
        return Result.success(service.localSnapshot());
    }

    @PostMapping("/sys/monitor/internal/slow-sql/threshold")
    @SaCheckPermission(SlowSqlPermission.CONFIG)
    public Result<SlowSqlSnapshotVO> localUpdateThreshold(@RequestBody @Valid SlowSqlCommandForm form) {
        return Result.success(service.localUpdateThreshold(form));
    }

    @PostMapping("/sys/monitor/internal/slow-sql/clear")
    @SaCheckPermission(SlowSqlPermission.CLEAR)
    public Result<SlowSqlSnapshotVO> localClear() {
        return Result.success(service.localClear());
    }
}
