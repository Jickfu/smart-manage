package sm.domain.sys.monitor.sql.service;

import sm.domain.sys.monitor.sql.converter.SqlLogConverter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.system.security.context.CurrentUserContext;
import sm.system.security.authorization.AdministratorOnly;
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
import sm.system.query.ListQueryUtil;

import java.util.List;
import java.util.Set;
import java.util.Map;

/** SQL 控制台公开业务入口。 */
@Service
@AdministratorOnly
@RequiredArgsConstructor
public class SqlService {
    static final String MAX_ROWS_PARAMETER = "SQL_CONSOLE_MAX_ROWS";
    static final int DEFAULT_MAX_ROWS = 1000;
    static final int HARD_MAX_ROWS = 5000;
    private static final Set<String> RESULT_TYPES = Set.of("QUERY", "DML", "DDL", "ERROR");
    private static final Map<String, ListQueryUtil.Field<SqlLogEntity>> LIST_FIELDS = Map.of(
            "id", ListQueryUtil.number(SqlLogEntity::getId, true),
            "sqlText", ListQueryUtil.string(SqlLogEntity::getSqlText, false),
            "resultType", ListQueryUtil.enumeration(SqlLogEntity::getResultType, false),
            "rowCount", ListQueryUtil.number(SqlLogEntity::getRowCount, true),
            "executeDuration", ListQueryUtil.number(SqlLogEntity::getExecuteDuration, true),
            "createName", ListQueryUtil.string(SqlLogEntity::getCreateName, false),
            "createIp", ListQueryUtil.string(SqlLogEntity::getCreateIp, false),
            "createTime", ListQueryUtil.dateTime(SqlLogEntity::getCreateTime, true));

    private final SqlLogMapper sqlLogMapper;
    private final SqlExecutionTxService executionTxService;
    private final SqlLogConverter converter;
    private final CurrentUserContext currentUserContext;
    private final SysParamService sysParamService;
    private final ClientIpResolver clientIpResolver;

    @BizLog(value = "执行SQL", recordRequest = false, recordResponse = false)
    public SqlResultVO execute(SqlExecuteForm form) {
        String sql = form.getSql().trim();
        SqlExecutionPlan plan = SqlExecutionPlan.parse(sql);
        SqlLogEntity logEntity = new SqlLogEntity();
        logEntity.setSqlText(sql);
        logEntity.setCreateName(currentUserContext.getUsernameOrDefault("未知"));
        logEntity.setCreateIp(resolveClientIp());
        return executionTxService.execute(plan, resolveMaxRows(), logEntity);
    }

    public PageData<SqlLogListVO> listPage(SqlLogListForm form) {
        validateListForm(form);
        LambdaQueryWrapper<SqlLogEntity> query = new LambdaQueryWrapper<>();
        query.select(SqlLogEntity::getId, SqlLogEntity::getSqlText, SqlLogEntity::getExecuteDuration,
                        SqlLogEntity::getResultType, SqlLogEntity::getRowCount, SqlLogEntity::getCreateName,
                        SqlLogEntity::getCreateIp, SqlLogEntity::getCreateTime)
                .like(StringUtil.isNotBlank(form.getKeyword()), SqlLogEntity::getSqlText, form.getKeyword())
                .eq(StringUtil.isNotBlank(form.getResultType()), SqlLogEntity::getResultType, form.getResultType())
                .ge(form.getStartTime() != null, SqlLogEntity::getCreateTime, form.getStartTime())
                .le(form.getEndTime() != null, SqlLogEntity::getCreateTime, form.getEndTime());
        ListQueryUtil.apply(query, form, LIST_FIELDS);
        if (!ListQueryUtil.hasSort(form)) query.orderByDesc(SqlLogEntity::getId);
        else if (!ListQueryUtil.isSortedBy(form, "id")) query.orderByDesc(SqlLogEntity::getId);
        Page<SqlLogEntity> page = sqlLogMapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), query);
        List<SqlLogListVO> records = page.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public SqlLogDetailVO detail(Long id) {
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
