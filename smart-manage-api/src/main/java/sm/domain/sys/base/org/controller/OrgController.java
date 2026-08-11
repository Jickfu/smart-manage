package sm.domain.sys.base.org.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.org.constant.OrgPermission;
import sm.domain.sys.base.org.model.form.OrgListForm;
import sm.domain.sys.base.org.model.form.OrgParentListForm;
import sm.domain.sys.base.org.model.form.OrgSaveForm;
import sm.domain.sys.base.org.model.vo.OrgDetailVO;
import sm.domain.sys.base.org.model.vo.OrgListVO;
import sm.domain.sys.base.org.model.vo.OrgOptionVO;
import sm.domain.sys.base.org.model.vo.OrgTreeVO;
import sm.domain.sys.base.org.service.OrgService;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrgController {
    private final OrgService service;

    @PostMapping("/sys/base/org/listPage")
    @SaCheckPermission(OrgPermission.LIST)
    public Result<PageData<OrgListVO>> listPage(@RequestBody OrgListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/org/parentListPage")
    @SaCheckPermission(OrgPermission.DETAIL)
    public Result<PageData<OrgListVO>> parentListPage(@RequestBody OrgParentListForm form) {
        return Result.success(service.parentListPage(form));
    }

    @GetMapping("/sys/base/org/tree")
    @SaCheckPermission(OrgPermission.LIST)
    public Result<List<OrgTreeVO>> tree(@RequestParam(defaultValue = "false") boolean showArchived) {
        return Result.success(service.tree(showArchived));
    }

    @PostMapping("/sys/base/org/detail")
    @SaCheckPermission(OrgPermission.DETAIL)
    public Result<OrgDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/org/save")
    @SaCheckPermission(OrgPermission.SAVE)
    public Result<Long> save(@RequestBody @Valid OrgSaveForm form) {
        return Result.success(service.save(form));
    }

    @GetMapping("/sys/base/org/options")
    public Result<List<OrgOptionVO>> options() {
        return Result.success(service.options());
    }

    @PostMapping("/sys/base/org/enable")
    @SaCheckPermission(OrgPermission.ENABLE)
    public Result<String> enable(@RequestBody @Valid IdsForm form) {
        service.enable(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/org/disable")
    @SaCheckPermission(OrgPermission.DISABLE)
    public Result<String> disable(@RequestBody @Valid IdsForm form) {
        service.disable(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/org/archive")
    @SaCheckPermission(OrgPermission.ARCHIVE)
    public Result<String> archive(@RequestBody @Valid IdsForm form) {
        service.archive(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/org/unarchive")
    @SaCheckPermission(OrgPermission.UNARCHIVE)
    public Result<String> unarchive(@RequestBody @Valid IdsForm form) {
        service.unarchive(form.getIds());
        return Result.success();
    }
}
