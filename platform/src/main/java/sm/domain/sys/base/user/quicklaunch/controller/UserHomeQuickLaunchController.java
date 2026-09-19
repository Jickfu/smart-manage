package sm.domain.sys.base.user.quicklaunch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.user.quicklaunch.model.form.QuickLaunchSaveForm;
import sm.domain.sys.base.user.quicklaunch.model.form.QuickLaunchScopeForm;
import sm.domain.sys.base.user.quicklaunch.model.vo.QuickLaunchConfigurationVO;
import sm.domain.sys.base.user.quicklaunch.model.vo.QuickLaunchItemVO;
import sm.domain.sys.base.user.quicklaunch.service.UserHomeQuickLaunchService;
import sm.system.response.Result;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "首页快速发起", description = "当前登录用户的首页快捷菜单配置")
public class UserHomeQuickLaunchController {
    private final UserHomeQuickLaunchService service;

    @PostMapping("/sys/base/user/current/home-quick-launch/list")
    @Operation(summary = "首页快速发起列表")
    public Result<List<QuickLaunchItemVO>> list(@RequestBody @Valid QuickLaunchScopeForm form) {
        return Result.success(service.list(form));
    }

    @PostMapping("/sys/base/user/current/home-quick-launch/configuration")
    @Operation(summary = "首页快速发起候选配置")
    public Result<QuickLaunchConfigurationVO> configuration(
            @RequestBody @Valid QuickLaunchScopeForm form) {
        return Result.success(service.configuration(form));
    }

    @PostMapping("/sys/base/user/current/home-quick-launch/save")
    @Operation(summary = "保存首页快速发起")
    public Result<String> save(@RequestBody @Valid QuickLaunchSaveForm form) {
        service.save(form);
        return Result.success();
    }
}
