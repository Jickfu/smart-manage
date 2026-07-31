package sm.domain.sys.scheduler.home.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.scheduler.home.service.SchedulerHomeService;
import sm.domain.sys.scheduler.model.vo.SchedulerSummaryVO;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@Tag(name = "任务调度-首页")
public class SchedulerHomeController {

    private final SchedulerHomeService service;

    @GetMapping("/sys/scheduler/home/summary")
    @Operation(summary = "调度首页统计", description = "获取任务状态和近七日执行趋势")
    @SaCheckPermission("sys:scheduler:job:listPage")
    public Result<SchedulerSummaryVO> summary() {
        return Result.success(service.summary());
    }
}
