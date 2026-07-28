package sm.domain.sys.monitor.operatelog.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.operatelog.model.form.OperateLogListForm;
import sm.domain.sys.monitor.operatelog.model.vo.OperateLogDetailVO;
import sm.domain.sys.monitor.operatelog.model.vo.OperateLogListVO;
import sm.domain.sys.monitor.operatelog.service.OperateLogQueryService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@Tag(name = "系统服务-操作日志", description = "操作日志查询")
@RequiredArgsConstructor
public class OperateLogController {
	private final OperateLogQueryService service;

	@PostMapping("/sys/log/operate/listPage")
	@Operation(summary = "操作日志分页")
	@SaCheckPermission("sys:log:operate:listPage")
	public Result<PageData<OperateLogListVO>> listPage(@Valid @RequestBody OperateLogListForm form) {
		return Result.success(service.listPage(form));
	}

	@PostMapping("/sys/log/operate/detail")
	@Operation(summary = "操作日志详情")
	@SaCheckPermission("sys:log:operate:detail")
	public Result<OperateLogDetailVO> detail(@Valid @RequestBody IdForm form) {
		return Result.success(service.detail(form.getId()));
	}
}

