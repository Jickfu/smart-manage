package sm.domain.sys.monitor.thread.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.thread.constant.ThreadDiagnosticPermission;
import sm.domain.sys.monitor.thread.model.form.ThreadCollectForm;
import sm.domain.sys.monitor.thread.model.vo.ThreadDiagnosticVO;
import sm.domain.sys.monitor.thread.service.ThreadDiagnosticService;
import sm.system.response.Result;

/** 指定应用实例的 JDK 原生线程诊断接口。 */
@RestController
@Tag(name = "系统监控-线程诊断", description = "线程列表、热点、堆栈和死锁诊断")
@RequiredArgsConstructor
public class ThreadDiagnosticController {
    private final ThreadDiagnosticService service;

    @GetMapping("/sys/monitor/thread/list")
    @Operation(summary = "线程列表")
    @SaCheckPermission(ThreadDiagnosticPermission.ACCESS)
    public Result<ThreadDiagnosticVO> list(@RequestParam(required = false) String instanceId) {
        return Result.success(service.list(instanceId));
    }

    @GetMapping("/sys/monitor/thread/{threadId}")
    @Operation(summary = "指定线程堆栈")
    @SaCheckPermission(ThreadDiagnosticPermission.ACCESS)
    public Result<ThreadDiagnosticVO> detail(@RequestParam(required = false) String instanceId,
                                             @PathVariable long threadId,
                                             @RequestParam(defaultValue = "128") int maxDepth) {
        return Result.success(service.detail(instanceId, threadId, maxDepth));
    }

    @PostMapping("/sys/monitor/thread/hot")
    @Operation(summary = "热点线程采样")
    @SaCheckPermission(ThreadDiagnosticPermission.COLLECT)
    public Result<ThreadDiagnosticVO> hot(@RequestBody @Valid ThreadCollectForm form) {
        return Result.success(service.hot(form));
    }

    @PostMapping("/sys/monitor/thread/dump")
    @Operation(summary = "全量线程快照")
    @SaCheckPermission(ThreadDiagnosticPermission.COLLECT)
    public Result<ThreadDiagnosticVO> dump(@RequestBody @Valid ThreadCollectForm form) {
        return Result.success(service.dump(form));
    }

    @PostMapping("/sys/monitor/thread/deadlocks")
    @Operation(summary = "死锁检测")
    @SaCheckPermission(ThreadDiagnosticPermission.COLLECT)
    public Result<ThreadDiagnosticVO> deadlocks(@RequestBody @Valid ThreadCollectForm form) {
        return Result.success(service.deadlocks(form));
    }

    @GetMapping("/sys/monitor/internal/thread/list")
    @SaCheckPermission(ThreadDiagnosticPermission.ACCESS)
    public Result<ThreadDiagnosticVO> localList() {
        return Result.success(service.localList());
    }

    @GetMapping("/sys/monitor/internal/thread/{threadId}")
    @SaCheckPermission(ThreadDiagnosticPermission.ACCESS)
    public Result<ThreadDiagnosticVO> localDetail(@PathVariable long threadId,
                                                  @RequestParam(defaultValue = "128") int maxDepth) {
        return Result.success(service.localDetail(threadId, maxDepth));
    }

    @PostMapping("/sys/monitor/internal/thread/hot")
    @SaCheckPermission(ThreadDiagnosticPermission.COLLECT)
    public Result<ThreadDiagnosticVO> localHot(@RequestBody @Valid ThreadCollectForm form) {
        return Result.success(service.localHot(form));
    }

    @PostMapping("/sys/monitor/internal/thread/dump")
    @SaCheckPermission(ThreadDiagnosticPermission.COLLECT)
    public Result<ThreadDiagnosticVO> localDump(@RequestBody @Valid ThreadCollectForm form) {
        return Result.success(service.localDump(form));
    }

    @PostMapping("/sys/monitor/internal/thread/deadlocks")
    @SaCheckPermission(ThreadDiagnosticPermission.COLLECT)
    public Result<ThreadDiagnosticVO> localDeadlocks(@RequestBody @Valid ThreadCollectForm form) {
        return Result.success(service.localDeadlocks(form));
    }
}
