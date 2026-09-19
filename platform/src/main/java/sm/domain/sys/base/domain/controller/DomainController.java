package sm.domain.sys.base.domain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.base.domain.constant.DomainPermission;
import sm.domain.sys.base.domain.model.form.DomainListForm;
import sm.domain.sys.base.domain.model.form.DomainSelectForm;
import sm.domain.sys.base.domain.model.form.DomainSaveForm;
import sm.domain.sys.base.domain.model.vo.DomainCreateNewDataVO;
import sm.domain.sys.base.domain.model.vo.DomainDetailVO;
import sm.domain.sys.base.domain.model.vo.DomainListVO;
import sm.domain.sys.base.domain.model.vo.DomainSelectVO;
import sm.domain.sys.base.domain.service.DomainService;
import sm.system.form.IdForm;
import sm.system.form.IdsForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@Tag(name = "系统管理-领域管理", description = "领域信息管理接口")
@RequiredArgsConstructor
public class DomainController {
	private final DomainService service;

	@Operation(summary = "领域列表", description = "获取领域分页列表数据")
	@PostMapping("/sys/base/domain/listPage")
	@SaCheckPermission(DomainPermission.LIST)
	public Result<PageData<DomainListVO>> listPage(@RequestBody DomainListForm form) {
		return Result.success(service.listPage(form));
	}

	@Operation(summary = "领域选择", description = "基础资料选择：获取领域分页列表数据")
	@PostMapping("/sys/base/domain/select")
	@SaCheckPermission(DomainPermission.SELECT)
	public Result<PageData<DomainSelectVO>> select(@RequestBody DomainSelectForm form) {
		return Result.success(service.select(form));
	}

	@Operation(summary = "领域详情", description = "按ID查询领域")
	@PostMapping("/sys/base/domain/detail")
	@SaCheckPermission(DomainPermission.DETAIL)
	public Result<DomainDetailVO> detail(@RequestBody @Valid IdForm form) {
		return Result.success(service.detail(form.getId()));
	}

	@Operation(summary = "保存领域", description = "新增或更新领域")
	@PostMapping("/sys/base/domain/save")
	@SaCheckPermission(DomainPermission.SAVE)
	public Result<Long> save(@Valid @RequestBody DomainSaveForm form) {
		return Result.success(service.save(form));
	}

	@GetMapping("/sys/base/domain/createNewData")
	@Operation(summary = "获取新增默认值", description = "获取领域新增时的默认初始数据")
	@SaCheckPermission(DomainPermission.SAVE)
	public Result<DomainCreateNewDataVO> createNewData() {
		return Result.success(service.createNewData());
	}

	@Operation(summary = "删除领域", description = "按ID删除领域")
	@PostMapping("/sys/base/domain/delete")
	@SaCheckPermission(DomainPermission.DELETE)
	public Result<String> delete(@RequestBody @Valid IdForm form) {
		service.deleteById(form.getId());
		return Result.success();
	}

	@PostMapping("/sys/base/domain/enable")
	@SaCheckPermission(DomainPermission.ENABLE)
	public Result<String> enable(@RequestBody @Valid IdsForm form) {
		service.enable(form.getIds());
		return Result.success();
	}

	@PostMapping("/sys/base/domain/disable")
	@SaCheckPermission(DomainPermission.DISABLE)
	public Result<String> disable(@RequestBody @Valid IdsForm form) {
		service.disable(form.getIds());
		return Result.success();
	}
}

