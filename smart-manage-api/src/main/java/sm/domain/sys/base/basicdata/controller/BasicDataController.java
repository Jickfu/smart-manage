package sm.domain.sys.base.basicdata.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.basicdata.model.form.BasicDataListForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataDeleteForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataNumberForm;
import sm.domain.sys.base.basicdata.model.form.BasicDataSaveForm;
import sm.domain.sys.base.basicdata.model.vo.BasicDataCreateNewDataVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataDetailVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataListVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataOptionVO;
import sm.domain.sys.base.basicdata.service.BasicDataService;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

import java.util.List;

/**
 * 基础数据管理
 *
 * @author Chekfu
 */
@RestController
@Tag(name = "系统建模-基础数据管理", description = "基础数据管理接口")
@RequiredArgsConstructor
public class BasicDataController {
    private final BasicDataService service;

    @PostMapping("/sys/base/basic-data/listPage")
    @Operation(summary = "基础数据列表", description = "获取基础数据分页列表")
    @SaCheckPermission("sys:base:basic-data:listPage")
    public Result<PageData<BasicDataListVO>> listPage(@RequestBody BasicDataListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/sys/base/basic-data/detail")
    @Operation(summary = "基础数据详情", description = "按ID查询基础数据")
    @SaCheckPermission("sys:base:basic-data:detail")
    public Result<BasicDataDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @PostMapping("/sys/base/basic-data/save")
    @Operation(summary = "保存基础数据", description = "新增或更新基础数据")
    @SaCheckPermission("sys:base:basic-data:save")
    public Result<Long> save(@Valid @RequestBody BasicDataSaveForm form) {
        return Result.success(service.save(form));
    }

    @GetMapping("/sys/base/basic-data/createNewData")
    @Operation(summary = "获取新增默认值", description = "获取基础数据新增时的默认初始数据")
    @SaCheckPermission("sys:base:basic-data:save")
    public Result<BasicDataCreateNewDataVO> createNewData() {
        return Result.success(service.createNewData());
    }

    @PostMapping("/sys/base/basic-data/delete")
    @Operation(summary = "删除基础数据", description = "按ID删除基础数据")
    @SaCheckPermission("sys:base:basic-data:delete")
    public Result<String> delete(@RequestBody @Valid BasicDataDeleteForm form) {
        service.delete(form);
        return Result.success();
    }

    @PostMapping("/sys/base/basic-data/enable")
    @SaCheckPermission("sys:base:basic-data:enable")
    public Result<String> enable(@RequestBody @Valid IdsForm form) {
        service.enable(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/basic-data/disable")
    @SaCheckPermission("sys:base:basic-data:disable")
    public Result<String> disable(@RequestBody @Valid IdsForm form) {
        service.disable(form.getIds());
        return Result.success();
    }

    @PostMapping("/sys/base/basic-data/options")
    @Operation(summary = "基础数据选项", description = "按基础数据编码获取启用的明细选项")
    public Result<List<BasicDataOptionVO>> options(@RequestBody @Valid BasicDataNumberForm form) {
        return Result.success(service.getOptionsByNumber(form.getNumber()));
    }
}
