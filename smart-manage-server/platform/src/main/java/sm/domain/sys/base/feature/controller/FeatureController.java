package sm.domain.sys.base.feature.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.feature.constant.FeaturePermission;
import sm.domain.sys.base.feature.model.form.FeatureListForm;
import sm.domain.sys.base.feature.model.form.FeatureSaveForm;
import sm.domain.sys.base.feature.model.vo.FeatureVO;
import sm.domain.sys.base.feature.service.FeatureService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeatureController {
    private final FeatureService service;

    @PostMapping("/sys/base/feature/listPage")
    @SaCheckPermission(FeaturePermission.LIST)
    public Result<PageData<FeatureVO>> listPage(@RequestBody FeatureListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/feature/listAllVisible")
    @SaCheckPermission(FeaturePermission.SELECT)
    public Result<List<FeatureVO>> listAllVisible() {
        return Result.success(service.listAllVisible());
    }

    @PostMapping("/sys/base/feature/detail")
    @SaCheckPermission(FeaturePermission.DETAIL)
    public Result<FeatureVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/feature/save")
    @SaCheckPermission(FeaturePermission.SAVE)
    public Result<String> save(@RequestBody @Valid FeatureSaveForm form) {
        service.save(form);
        return Result.success();
    }
}
