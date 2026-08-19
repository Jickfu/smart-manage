package sm.domain.sys.scheduler.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.scheduler.constant.JobPermission;
import sm.domain.sys.scheduler.model.form.JobListForm;
import sm.domain.sys.scheduler.model.form.JobCommandForm;
import sm.domain.sys.scheduler.model.form.JobBatchCommandForm;
import sm.domain.sys.scheduler.model.form.JobSaveForm;
import sm.domain.sys.scheduler.model.form.CronPreviewForm;
import sm.domain.sys.scheduler.model.vo.JobDetailVO;
import sm.domain.sys.scheduler.model.vo.JobListVO;
import sm.domain.sys.scheduler.service.JobService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;
import java.util.Map;

/**
 * 定时任务管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "任务调度-定时任务", description = "定时任务管理接口")
@RequiredArgsConstructor
public class JobController {

    private final JobService service;

    @PostMapping("/sys/scheduler/job/listPage")
    @Operation(summary = "任务列表", description = "获取任务分页列表")
    @SaCheckPermission(JobPermission.LIST)
    public Result<PageData<JobListVO>> listPage(@RequestBody JobListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/scheduler/job/detail")
    @Operation(summary = "任务详情", description = "按ID查询任务详情")
    @SaCheckPermission(JobPermission.DETAIL)
    public Result<JobDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/scheduler/job/save")
    @Operation(summary = "保存任务", description = "新增或更新任务，同步到 Quartz 调度器")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<Long> save(@RequestBody @Valid JobSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/scheduler/job/delete")
    @Operation(summary = "删除任务", description = "按ID删除任务，同时从 Quartz 移除")
    @SaCheckPermission(JobPermission.DELETE)
    public Result<String> delete(@RequestBody @Valid JobCommandForm form) {
        service.deleteById(form.getId(), form.getVersion());
        return Result.success();
    }

    @PostMapping("/sys/scheduler/job/pause")
    @Operation(summary = "暂停任务", description = "暂停指定任务")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<String> pause(@RequestBody @Valid JobBatchCommandForm form) {
        service.pause(form.getJobs());
        return Result.success();
    }

    @PostMapping("/sys/scheduler/job/resume")
    @Operation(summary = "恢复任务", description = "恢复已暂停的任务")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<String> resume(@RequestBody @Valid JobBatchCommandForm form) {
        service.resume(form.getJobs());
        return Result.success();
    }

    @PostMapping("/sys/scheduler/job/trigger")
    @Operation(summary = "立即执行", description = "手动触发一次任务执行")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<String> trigger(@RequestBody @Valid IdForm form) {
        service.trigger(form.getId());
        return Result.success();
    }

    @PostMapping("/sys/scheduler/job/syncAll")
    @Operation(summary = "重新同步", description = "以数据库为准重新同步全部 Quartz 任务并清理孤儿任务")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<String> syncAll() {
        service.syncAll();
        return Result.success();
    }

    @PostMapping("/sys/scheduler/job/classes")
    @Operation(summary = "可用Job类", description = "获取所有实现了 org.quartz.Job 接口的类列表")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<List<Map<String, String>>> classes() {
        return Result.success(service.getAvailableJobClasses());
    }

    @PostMapping("/sys/scheduler/job/cronPreview")
    @Operation(summary = "Cron 预览", description = "按服务端时区返回未来五次触发时间")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<List<java.time.LocalDateTime>> cronPreview(@RequestBody @Valid CronPreviewForm form) {
        return Result.success(service.previewCron(form.getCronExpression()));
    }

    @GetMapping("/sys/scheduler/job/createNewData")
    @Operation(summary = "新建默认值", description = "获取新建任务时的默认值")
    @SaCheckPermission(JobPermission.SAVE)
    public Result<Map<String, Object>> createNewData() {
        return Result.success(service.createNewData());
    }
}
