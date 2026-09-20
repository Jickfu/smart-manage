package sm.domain.sys.monitor.sql.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.sys.monitor.sql.constant.SqlPermission;
import sm.domain.sys.monitor.sql.model.form.SqlExecuteForm;
import sm.domain.sys.monitor.sql.model.form.SqlLogListForm;
import sm.domain.sys.monitor.sql.model.vo.SqlLogDetailVO;
import sm.domain.sys.monitor.sql.model.vo.SqlLogListVO;
import sm.domain.sys.monitor.sql.model.vo.SqlResultVO;
import sm.domain.sys.monitor.sql.service.SqlService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

/**
 * SQL 控制台
 */
@RestController
@Tag(name = "SQL 控制台", description = "SQL 执行与查询")
@RequiredArgsConstructor
public class SqlController {

    private final SqlService sqlService;

    @PostMapping("/sys/monitor/sql/execute")
    @Operation(summary = "执行 SQL 语句")
    @SaCheckPermission(SqlPermission.EXECUTE)
    public Result<SqlResultVO> execute(@Valid @RequestBody SqlExecuteForm form) {
        return Result.success(sqlService.execute(form));
    }

    @PostMapping("/sys/monitor/sql/log/listPage")
    @Operation(summary = "执行日志分页查询")
    @SaCheckPermission(SqlPermission.LOG_LIST)
    public Result<PageData<SqlLogListVO>> listPage(@Valid @RequestBody SqlLogListForm form) {
        return Result.success(sqlService.listPage(form));
    }

    @PostMapping("/sys/monitor/sql/log/detail")
    @Operation(summary = "执行日志详情")
    @SaCheckPermission(SqlPermission.LOG_DETAIL)
    public Result<SqlLogDetailVO> detail(@Valid @RequestBody IdForm form) {
        return Result.success(sqlService.detail(form.getId()));
    }
}
