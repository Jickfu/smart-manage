package sm.domain.sys.monitor.sql.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.monitor.sql.mapper.SqlLogMapper;
import sm.domain.sys.monitor.sql.model.entity.SqlLogEntity;
import sm.domain.sys.monitor.sql.model.form.SqlExecuteForm;
import sm.domain.sys.monitor.sql.model.form.SqlLogListForm;
import sm.domain.sys.monitor.sql.model.vo.SqlLogDetailVO;
import sm.domain.sys.monitor.sql.model.vo.SqlLogListVO;
import sm.domain.sys.monitor.sql.model.vo.SqlResultVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.web.ClientIpResolver;
import sm.system.util.StringUtil;
import sm.domain.sys.monitor.common.util.LogQueryValidator;

import java.util.List;
import java.util.Set;

/** SQL 控制台公开业务入口。 */
@Service
@RequiredArgsConstructor
public class SqlService {
    static final String MAX_ROWS_PARAMETER = "SQL_CONSOLE_MAX_ROWS";
    static final int DEFAULT_MAX_ROWS = 1000;
    static final int HARD_MAX_ROWS = 5000;
    private static final Set<String> RESULT_TYPES = Set.of("QUERY", "DML", "DDL", "ERROR");

    private final SqlLogMapper sqlLogMapper;
    private final SqlExecutionTxService executionTxService;
    private final SqlLogConverter converter;
    private final CurrentUserContext currentUserContext;
    private final SysParamService sysParamService;
    private final ClientIpResolver clientIpResolver;

    @BizLog(value = "执行SQL", recordRequest = false, recordResponse = false)
    public SqlResultVO execute(SqlExecuteForm form) {
        currentUserContext.checkAdministrator();
        String sql = form.getSql().trim();
        SqlExecutionPlan plan = SqlExecutionPlan.parse(sql);
        SqlLogEntity logEntity = new SqlLogEntity();
        logEntity.setSqlText(sql);
        logEntity.setCreateName(currentUserContext.getUsernameOrDefault("未知"));
        logEntity.setCreateIp(resolveClientIp());
        return executionTxService.execute(plan, resolveMaxRows(), logEntity);
    }

    public PageData<SqlLogListVO> listPage(SqlLogListForm form) {
        currentUserContext.checkAdministrator();
        validateListForm(form);
        LambdaQueryWrapper<SqlLogEntity> query = new LambdaQueryWrapper<>();
        query.select(SqlLogEntity::getId, SqlLogEntity::getSqlText, SqlLogEntity::getExecuteDuration,
                        SqlLogEntity::getResultType, SqlLogEntity::getRowCount, SqlLogEntity::getCreateName,
                        SqlLogEntity::getCreateIp, SqlLogEntity::getCreateTime)
                .like(StringUtil.isNotBlank(form.getKeyword()), SqlLogEntity::getSqlText, form.getKeyword())
                .eq(StringUtil.isNotBlank(form.getResultType()), SqlLogEntity::getResultType, form.getResultType())
                .ge(form.getStartTime() != null, SqlLogEntity::getCreateTime, form.getStartTime())
                .le(form.getEndTime() != null, SqlLogEntity::getCreateTime, form.getEndTime())
                .orderByDesc(SqlLogEntity::getId);
        Page<SqlLogEntity> page = sqlLogMapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), query);
        List<SqlLogListVO> records = page.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public SqlLogDetailVO detail(Long id) {
        currentUserContext.checkAdministrator();
        SqlLogEntity entity = sqlLogMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "执行日志不存在");
        }
        return converter.toDetailVO(entity);
    }

    int resolveMaxRows() {
        Integer configured = sysParamService.getInt(MAX_ROWS_PARAMETER);
        int maxRows = configured == null ? DEFAULT_MAX_ROWS : configured;
        if (maxRows < 1 || maxRows > HARD_MAX_ROWS) {
            throw new BizException(ResultEnum.CONFIG_ERROR,
                    "系统参数 " + MAX_ROWS_PARAMETER + " 必须在 1～" + HARD_MAX_ROWS + " 之间");
        }
        return maxRows;
    }

    private void validateListForm(SqlLogListForm form) {
        LogQueryValidator.validateTimeRange(form.getStartTime(), form.getEndTime());
        if (StringUtil.isNotBlank(form.getResultType()) && !RESULT_TYPES.contains(form.getResultType())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "SQL 结果类型不合法");
        }
    }

    private String resolveClientIp() {
        try {
            return clientIpResolver.resolveCurrentRequest();
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
