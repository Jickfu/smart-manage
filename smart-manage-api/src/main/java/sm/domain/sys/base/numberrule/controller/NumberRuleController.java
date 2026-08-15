package sm.domain.sys.base.numberrule.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.numberrule.constant.NumberRulePermission;
import sm.domain.sys.base.numberrule.model.form.NumberRuleDeleteForm;
import sm.domain.sys.base.numberrule.model.form.NumberRuleListForm;
import sm.domain.sys.base.numberrule.model.form.NumberRulePreviewForm;
import sm.domain.sys.base.numberrule.model.form.NumberRuleSaveForm;
import sm.domain.sys.base.numberrule.model.vo.NumberRuleOptionVO;
import sm.domain.sys.base.numberrule.model.vo.NumberReferenceVO;
import sm.domain.sys.base.numberrule.model.vo.NumberRuleVO;
import sm.domain.sys.base.numberrule.service.NumberRuleService;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "系统建模-编号规则", description = "编号规则管理接口")
public class NumberRuleController {
    private final NumberRuleService service;

    @PostMapping("/sys/base/number-rule/listPage")
    @SaCheckPermission(NumberRulePermission.LIST)
    @Operation(summary = "编号规则列表")
    public Result<PageData<NumberRuleVO>> listPage(@RequestBody NumberRuleListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/number-rule/detail")
    @SaCheckPermission(NumberRulePermission.DETAIL)
    @Operation(summary = "编号规则详情")
    public Result<NumberRuleVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/number-rule/save")
    @SaCheckPermission(NumberRulePermission.SAVE)
    @Operation(summary = "保存编号规则")
    public Result<Long> save(@RequestBody @Valid NumberRuleSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/base/number-rule/delete")
    @SaCheckPermission(NumberRulePermission.DELETE)
    @Operation(summary = "删除编号规则")
    public Result<String> delete(@RequestBody @Valid NumberRuleDeleteForm form) {
        service.delete(form);
        return Result.success();
    }

    @PostMapping("/sys/base/number-rule/enable")
    @SaCheckPermission(NumberRulePermission.ENABLE)
    public Result<String> enable(@RequestBody @Valid IdsForm form) {
        service.enable(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/number-rule/disable")
    @SaCheckPermission(NumberRulePermission.DISABLE)
    public Result<String> disable(@RequestBody @Valid IdsForm form) {
        service.disable(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/number-rule/setDefault")
    @SaCheckPermission(NumberRulePermission.SAVE)
    public Result<String> setDefault(@RequestBody @Valid IdForm form) {
        service.setDefault(form.getId());
        return Result.success();
    }

    @GetMapping("/sys/base/number-rule/references")
    @SaCheckPermission(NumberRulePermission.LIST)
    @Operation(summary = "编号引用及受控变量")
    public Result<List<NumberReferenceVO>> references() {
        return Result.success(service.references());
    }

    @GetMapping("/sys/base/number-rule/options")
    @SaCheckPermission(NumberRulePermission.SELECT)
    @Operation(summary = "编号规则选项")
    public Result<List<NumberRuleOptionVO>> options(@RequestParam(required = false) String scopeType,
                                                    @RequestParam(required = false) String referenceKey) {
        return Result.success(service.options(scopeType, referenceKey));
    }

    @PostMapping("/sys/base/number-rule/preview")
    @SaCheckPermission(NumberRulePermission.PREVIEW)
    @Operation(summary = "预览编号模板")
    public Result<String> preview(@RequestBody @Valid NumberRulePreviewForm form) {
        return Result.success(service.preview(form));
    }
}
