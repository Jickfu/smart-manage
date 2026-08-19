package sm.domain.sys.scheduler.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.scheduler.constant.JobExecutionPermission;
import sm.domain.sys.scheduler.model.form.JobLogListForm;
import sm.domain.sys.scheduler.model.vo.JobLogListVO;
import sm.domain.sys.scheduler.model.vo.JobLogDetailVO;
import sm.domain.sys.scheduler.service.JobLogService;
import sm.system.form.IdForm;
import jakarta.validation.Valid;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

/**
 * 执行实例/执行日志
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "任务调度-执行记录", description = "执行实例查询接口")
@RequiredArgsConstructor
public class JobLogController {

    private final JobLogService service;

    @PostMapping("/sys/scheduler/execution/listPage")
    @Operation(summary = "执行实例列表", description = "获取任务执行实例分页列表，支持按状态筛选")
    @SaCheckPermission(JobExecutionPermission.LIST)
    public Result<PageData<JobLogListVO>> listPage(@RequestBody JobLogListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/scheduler/execution/running")
    @Operation(summary = "正在运行的实例", description = "查询当前状态为 RUNNING 的执行实例")
    @SaCheckPermission(JobExecutionPermission.LIST)
    public Result<List<JobLogListVO>> running() {
        return Result.success(service.running());
    }

    @PostMapping("/sys/scheduler/execution/detail")
    @Operation(summary = "执行实例详情", description = "按ID查询任务执行实例详情")
    @SaCheckPermission(JobExecutionPermission.DETAIL)
    public Result<JobLogDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }
}
