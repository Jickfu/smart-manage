package sm.domain.sys.scheduler.singleton.controller;

import java.util.List;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.sys.scheduler.singleton.constant.SingletonPermission;
import sm.domain.sys.scheduler.singleton.model.form.*;
import sm.domain.sys.scheduler.singleton.model.vo.*;
import sm.domain.sys.scheduler.singleton.service.SingletonTaskService;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@RequestMapping("/sys/scheduler/singleton")
@RequiredArgsConstructor
@Tag(name = "任务调度-常驻任务")
public class SingletonTaskController {
    private final SingletonTaskService service;
    @PostMapping("/listPage")
    @Operation(summary = "常驻任务列表")
    @SaCheckPermission(SingletonPermission.LIST)
    public Result<PageData<SingletonTaskVO>> listPage(@RequestBody @Valid SingletonListForm form) {
        return Result.success(service.listPage(form));
    }
    @PostMapping("/detail")
    @Operation(summary = "常驻任务详情")
    @SaCheckPermission(SingletonPermission.DETAIL)
    public Result<SingletonTaskVO> detail(@RequestBody @Valid SingletonDetailForm form) {
        return Result.success(service.detail(form.getNumber()));
    }
    @PostMapping("/events")
    @Operation(summary = "常驻任务近期事件")
    @SaCheckPermission(SingletonPermission.DETAIL)
    public Result<List<SingletonEventVO>> events(@RequestBody @Valid SingletonDetailForm form) {
        return Result.success(service.events(form.getNumber()));
    }
    @PostMapping("/control")
    @Operation(summary = "设置常驻任务运行期望")
    @SaCheckPermission(SingletonPermission.CONTROL)
    public Result<String> control(@RequestBody @Valid SingletonCommandForm form) {
        service.control(form);
        return Result.success();
    }
}
