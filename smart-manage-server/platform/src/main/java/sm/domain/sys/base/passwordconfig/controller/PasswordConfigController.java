package sm.domain.sys.base.passwordconfig.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sm.domain.sys.base.passwordconfig.constant.PasswordConfigPermission;
import sm.domain.sys.base.passwordconfig.model.form.PasswordConfigSaveForm;
import sm.domain.sys.base.passwordconfig.model.vo.PasswordConfigDetailVO;
import sm.domain.sys.base.passwordconfig.model.vo.PasswordPolicyVO;
import sm.domain.sys.base.passwordconfig.service.PasswordConfigService;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
public class PasswordConfigController {
    private final PasswordConfigService service;

    @GetMapping("/sys/base/password-config/singleton")
    @SaCheckPermission(PasswordConfigPermission.DETAIL)
    public Result<PasswordConfigDetailVO> singleton(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return Result.success(service.singleton());
    }

    @PostMapping("/sys/base/password-config/save")
    @SaCheckPermission(PasswordConfigPermission.SAVE)
    public Result<Long> save(@Valid @RequestBody PasswordConfigSaveForm form) {
        return Result.success(service.save(form));
    }

    /** 首次改密和找回密码也需读取规则；不提供匿名候选密码检查。 */
    @SaIgnore
    @GetMapping("/sys/base/login/password/policy")
    public Result<PasswordPolicyVO> policy(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return Result.success(service.policy());
    }
}
