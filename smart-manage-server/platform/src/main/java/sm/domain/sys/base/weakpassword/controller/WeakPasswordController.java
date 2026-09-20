package sm.domain.sys.base.weakpassword.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.weakpassword.constant.WeakPasswordPermission;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordDeleteForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordListForm;
import sm.domain.sys.base.weakpassword.model.form.WeakPasswordSaveForm;
import sm.domain.sys.base.weakpassword.model.vo.WeakPasswordVO;
import sm.domain.sys.base.weakpassword.service.WeakPasswordService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
public class WeakPasswordController {
    private final WeakPasswordService service;

    @PostMapping("/sys/base/weak-password/listPage")
    @SaCheckPermission(WeakPasswordPermission.LIST)
    public Result<PageData<WeakPasswordVO>> listPage(@RequestBody WeakPasswordListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/weak-password/detail")
    @SaCheckPermission(WeakPasswordPermission.DETAIL)
    public Result<WeakPasswordVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/weak-password/save")
    @SaCheckPermission(WeakPasswordPermission.SAVE)
    public Result<Long> save(@RequestBody @Valid WeakPasswordSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/base/weak-password/delete")
    @SaCheckPermission(WeakPasswordPermission.DELETE)
    public Result<String> delete(@RequestBody @Valid WeakPasswordDeleteForm form) {
        service.delete(form);
        return Result.success();
    }
}
