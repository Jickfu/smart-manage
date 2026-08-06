package sm.domain.sys.monitor.arthas.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.arthas.model.form.ArthasExecuteForm;
import sm.domain.sys.monitor.arthas.model.vo.ArthasResultVO;
import sm.domain.sys.monitor.arthas.service.ArthasService;
import sm.system.response.Result;

/**
 * Arthas 诊断工具接口
 */
@RestController
@Tag(name = "诊断工具-Arthas", description = "Arthas Java 诊断命令代理")
@RequiredArgsConstructor
public class ArthasController {

    private final ArthasService service;

    @PostMapping("/sys/monitor/arthas/execute")
    @Operation(summary = "执行一次性命令", description = "执行 thread/jvm/memory/dashboard/vmoption/logger 等一次性命令")
    @SaCheckPermission("sys:monitor:arthas:execute")
    public Result<ArthasResultVO> execute(@RequestBody @Valid ArthasExecuteForm form) {
        return Result.success(service.execute(form.getCommand(), form.getArgs()));
    }

    @PostMapping("/sys/monitor/arthas/start")
    @Operation(summary = "启动持续命令", description = "启动 trace/watch/stack/tt/monitor 等持续命令，返回会话 ID")
    @SaCheckPermission("sys:monitor:arthas:execute")
    public Result<ArthasResultVO> start(@RequestBody @Valid ArthasExecuteForm form) {
        return Result.success(service.start(form.getCommand(), form.getArgs()));
    }

    @PostMapping("/sys/monitor/arthas/stop")
    @Operation(summary = "停止持续命令", description = "按会话 ID 停止正在执行的持续命令")
    @SaCheckPermission("sys:monitor:arthas:execute")
    public Result<ArthasResultVO> stop(@RequestBody @Valid SessionForm form) {
        return Result.success(service.stop(form.getInstanceId(), form.getSessionId()));
    }

    @PostMapping("/sys/monitor/arthas/read")
    @Operation(summary = "读取命令输出", description = "读取持续命令的当前输出")
    @SaCheckPermission("sys:monitor:arthas:execute")
    public Result<ArthasResultVO> read(@RequestBody @Valid SessionForm form) {
        return Result.success(service.read(form.getInstanceId(), form.getSessionId()));
    }

    @Data
    static class SessionForm {
        @NotBlank(message = "会话ID不能为空")
        @Parameter(description = "会话 ID")
        private String sessionId;

        @NotBlank(message = "实例ID不能为空")
        @Parameter(description = "start 返回的应用实例 ID")
        private String instanceId;
    }
}
