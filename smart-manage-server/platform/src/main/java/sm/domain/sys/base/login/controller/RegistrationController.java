package sm.domain.sys.base.login.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.login.model.form.RegistrationCodeForm;
import sm.domain.sys.base.login.model.form.RegistrationForm;
import sm.domain.sys.base.login.service.LoginService;
import sm.domain.sys.base.user.service.UserRegistrationService;
import sm.system.response.Result;

@RestController
@RequestMapping("/sys/base/registration")
@Tag(name = "邮箱自助注册")
@RequiredArgsConstructor
public class RegistrationController {
    private final UserRegistrationService registrationService;
    private final LoginService loginService;

    @SaIgnore
    @GetMapping("/availability")
    public Result<Boolean> availability(jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return Result.success(registrationService.available());
    }

    @SaIgnore
    @PostMapping("/code")
    public Result<String> requestCode(@Valid @RequestBody RegistrationCodeForm form) {
        String clientIp = loginService.consumeRegistrationCaptcha(form.email(), form.captchaTicket());
        registrationService.requestCode(form.email(), clientIp);
        return Result.success(UserRegistrationService.SEND_MESSAGE);
    }

    @SaIgnore
    @PostMapping("/complete")
    public Result<String> register(@Valid @RequestBody RegistrationForm form) {
        registrationService.register(form.email(), form.code(), form.password());
        return Result.success();
    }
}
