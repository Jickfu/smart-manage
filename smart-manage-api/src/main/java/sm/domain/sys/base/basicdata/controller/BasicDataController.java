package sm.domain.sys.base.basicdata.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.basicdata.constant.BasicDataPermission;
import sm.domain.sys.base.basicdata.model.form.BasicDataCategorySaveForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataDeleteForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataItemSaveForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataListForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataNumberForm;
import sm.domain.sys.base.basicdata.model.vo.BasicDataCategoryVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataItemDetailVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataListVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataOptionVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataTreeVO;
import sm.domain.sys.base.basicdata.service.BasicDataService;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BasicDataController {
    private final BasicDataService service;

    @GetMapping("/sys/base/basic-data/categoryTree")
    @SaCheckPermission(BasicDataPermission.LIST)
    public Result<List<BasicDataTreeVO>> categoryTree() {
        return Result.success(service.categoryTree());
    }

    @PostMapping("/sys/base/basic-data/categoryDetail")
    @SaCheckPermission(BasicDataPermission.DETAIL)
    public Result<BasicDataCategoryVO> categoryDetail(@RequestBody @Valid IdForm form) {
        return Result.success(service.categoryDetail(form.getId()));
    }

    @PostMapping("/sys/base/basic-data/saveCategory")
    @SaCheckPermission(BasicDataPermission.SAVE)
    public Result<Long> saveCategory(@RequestBody @Valid BasicDataCategorySaveForm form) {
        return Result.success(service.saveCategory(form));
    }

    @PostMapping("/sys/base/basic-data/deleteCategory")
    @SaCheckPermission(BasicDataPermission.DELETE)
    public Result<String> deleteCategory(@RequestBody @Valid BasicDataDeleteForm form) {
        service.deleteCategory(form);
        return Result.success();
    }

    @PostMapping("/sys/base/basic-data/listPage")
    @SaCheckPermission(BasicDataPermission.LIST)
    public Result<PageData<BasicDataListVO>> listPage(@RequestBody BasicDataListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/basic-data/detail")
    @SaCheckPermission(BasicDataPermission.DETAIL)
    public Result<BasicDataItemDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/basic-data/save")
    @SaCheckPermission(BasicDataPermission.SAVE)
    public Result<Long> save(@RequestBody @Valid BasicDataItemSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/sys/base/basic-data/delete")
    @SaCheckPermission(BasicDataPermission.DELETE)
    public Result<String> delete(@RequestBody @Valid BasicDataDeleteForm form) {
        service.delete(form);
        return Result.success();
    }

    @PostMapping("/sys/base/basic-data/enable")
    @SaCheckPermission(BasicDataPermission.ENABLE)
    public Result<String> enable(@RequestBody @Valid IdsForm form) {
        service.enable(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/basic-data/disable")
    @SaCheckPermission(BasicDataPermission.DISABLE)
    public Result<String> disable(@RequestBody @Valid IdsForm form) {
        service.disable(form.getIds());
        return Result.success();
    }

    @GetMapping("/sys/base/basic-data/parentOptions")
    @SaCheckPermission(BasicDataPermission.DETAIL)
    public Result<List<BasicDataOptionVO>> parentOptions(@RequestParam Long categoryId,
                                                         @RequestParam(required = false) Long excludeId) {
        return Result.success(service.parentOptions(categoryId, excludeId));
    }

    @PostMapping("/sys/base/basic-data/options")
    public Result<List<BasicDataOptionVO>> options(@RequestBody @Valid BasicDataNumberForm form) {
        return Result.success(service.getOptionsByNumber(form.getNumber()));
    }
}
