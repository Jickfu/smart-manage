package sm.domain.sys.monitor.script.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.monitor.script.mapper.ScriptLogMapper;
import sm.domain.sys.monitor.script.mapper.ScriptMapper;
import sm.domain.sys.monitor.script.model.entity.ScriptEntity;
import sm.domain.sys.monitor.script.model.entity.ScriptLogEntity;
import sm.domain.sys.monitor.script.model.form.*;
import sm.domain.sys.monitor.script.model.vo.*;
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

/** 脚本控制台与脚本管理的唯一公开业务入口。 */
@Service
@RequiredArgsConstructor
public class ScriptService {
    static final String TIMEOUT_PARAMETER = "SCRIPT_CONSOLE_TIMEOUT_SECONDS";
    static final String MAX_SOURCE_PARAMETER = "SCRIPT_CONSOLE_MAX_SOURCE_LENGTH";
    static final String MAX_OUTPUT_PARAMETER = "SCRIPT_CONSOLE_MAX_OUTPUT_LENGTH";
    private static final Set<String> STATUSES = Set.of("SUCCESS", "ERROR", "TIMEOUT");
    private static final Set<String> TRANSACTION_MODES = Set.of("ATOMIC", "NON_ATOMIC");
    private static final Map<String, ListQueryUtil.Field<ScriptEntity>> SCRIPT_LIST_FIELDS = Map.of(
            "number", ListQueryUtil.string(ScriptEntity::getNumber, true),
            "name", ListQueryUtil.string(ScriptEntity::getName, true),
            "description", ListQueryUtil.string(ScriptEntity::getDescription, false),
            "createTime", ListQueryUtil.dateTime(ScriptEntity::getCreateTime, true),
            "updateTime", ListQueryUtil.dateTime(ScriptEntity::getUpdateTime, true));
    private static final Map<String, ListQueryUtil.Field<ScriptLogEntity>> LOG_LIST_FIELDS = Map.ofEntries(
            Map.entry("id", ListQueryUtil.number(ScriptLogEntity::getId, true)),
            Map.entry("scriptName", ListQueryUtil.string(ScriptLogEntity::getScriptName, false)),
            Map.entry("executeStatus", ListQueryUtil.enumeration(ScriptLogEntity::getExecuteStatus, false)),
            Map.entry("transactionMode", ListQueryUtil.enumeration(ScriptLogEntity::getTransactionMode, false)),
            Map.entry("transactionResult", ListQueryUtil.enumeration(ScriptLogEntity::getTransactionResult, false)),
            Map.entry("executeDuration", ListQueryUtil.number(ScriptLogEntity::getExecuteDuration, true)),
            Map.entry("createName", ListQueryUtil.string(ScriptLogEntity::getCreateName, false)),
            Map.entry("createIp", ListQueryUtil.string(ScriptLogEntity::getCreateIp, false)),
            Map.entry("createTime", ListQueryUtil.dateTime(ScriptLogEntity::getCreateTime, true)));

    private final ScriptMapper scriptMapper;
    private final ScriptLogMapper scriptLogMapper;
    private final ScriptTxService scriptTxService;
    private final ScriptExecutionTxService executionTxService;
    private final ScriptExecutionLogTxService executionLogTxService;
    private final ScriptExecutor scriptExecutor;
    private final ScriptServiceCatalog serviceCatalog;
    private final ScriptConverter converter;
    private final CurrentUserContext currentUserContext;
    private final SysParamService sysParamService;
    private final ClientIpResolver clientIpResolver;

    @BizLog(value = "执行脚本", recordRequest = false, recordResponse = false)
    public ScriptResultVO execute(ScriptExecuteForm form) {
        currentUserContext.checkAdministrator();
        String content = form.getContent().trim();
        ScriptExecutionConfig config = resolveExecutionConfig(content, form.getTransactionMode());
        ScriptEntity savedScript = form.getScriptId() == null ? null : requireScript(form.getScriptId());
        ScriptExecutionOutcome outcome;
        if ("ATOMIC".equals(config.transactionMode())) {
            try {
                outcome = executionTxService.execute(config, content);
            } catch (ScriptExecutionFailure failure) {
                outcome = failure.getOutcome();
            }
        } else {
            outcome = scriptExecutor.execute(config, content);
        }
        ScriptResultVO result = toResult(outcome, config.transactionMode());
        executionLogTxService.save(createLog(form, content, savedScript, result));
        return result;
    }

    public PageData<ScriptListVO> listPage(ScriptListForm form) {
        currentUserContext.checkAdministrator();
        LambdaQueryWrapper<ScriptEntity> query = new LambdaQueryWrapper<>();
        query.and(StringUtil.isNotBlank(form.getKeyword()), wrapper -> wrapper
                        .like(ScriptEntity::getNumber, form.getKeyword())
                        .or().like(ScriptEntity::getName, form.getKeyword()));
        ListQueryUtil.apply(query, form, SCRIPT_LIST_FIELDS);
        if (!ListQueryUtil.hasSort(form)) query.orderByDesc(ScriptEntity::getUpdateTime);
        if (!ListQueryUtil.isSortedBy(form, "id")) query.orderByDesc(ScriptEntity::getId);
        Page<ScriptEntity> page = scriptMapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), query);
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(),
                page.getRecords().stream().map(converter::toListVO).toList());
    }

    public ScriptDetailVO detail(Long id) {
        currentUserContext.checkAdministrator();
        return converter.toDetailVO(requireScript(id));
    }

    public ScriptDetailVO createNewData() {
        currentUserContext.checkAdministrator();
        ScriptDetailVO result = new ScriptDetailVO();
        result.setContent("console.log('Hello Smart Manage');\nreturn { success: true };");
        return result;
    }

    public List<ScriptApiServiceVO> apiMetadata() {
        currentUserContext.checkAdministrator();
        return serviceCatalog.metadata();
    }

    @BizLog(value = "保存脚本", recordRequest = false)
    public Long save(ScriptSaveForm form) {
        currentUserContext.checkAdministrator();
        validateSourceLength(form.getContent());
        return scriptTxService.save(form);
    }

    @BizLog("删除脚本")
    public void delete(ScriptDeleteForm form) {
        currentUserContext.checkAdministrator();
        scriptTxService.delete(form);
    }

    public PageData<ScriptLogListVO> logListPage(ScriptLogListForm form) {
        currentUserContext.checkAdministrator();
        validateLogForm(form);
        LambdaQueryWrapper<ScriptLogEntity> query = new LambdaQueryWrapper<>();
        // 源码、输出和错误正文仅在详情加载。
        query.select(ScriptLogEntity::getId, ScriptLogEntity::getScriptId, ScriptLogEntity::getScriptName,
                        ScriptLogEntity::getTransactionMode, ScriptLogEntity::getExecuteStatus,
                        ScriptLogEntity::getExecuteDuration, ScriptLogEntity::getTransactionResult,
                        ScriptLogEntity::getCreateName, ScriptLogEntity::getCreateIp, ScriptLogEntity::getCreateTime)
                .and(StringUtil.isNotBlank(form.getKeyword()), wrapper -> wrapper
                        .like(ScriptLogEntity::getScriptName, form.getKeyword())
                        .or().like(ScriptLogEntity::getScriptContent, form.getKeyword()))
                .eq(StringUtil.isNotBlank(form.getStatus()), ScriptLogEntity::getExecuteStatus, form.getStatus())
                .eq(StringUtil.isNotBlank(form.getTransactionMode()), ScriptLogEntity::getTransactionMode,
                        form.getTransactionMode())
                .ge(form.getStartTime() != null, ScriptLogEntity::getCreateTime, form.getStartTime())
                .le(form.getEndTime() != null, ScriptLogEntity::getCreateTime, form.getEndTime());
        ListQueryUtil.apply(query, form, LOG_LIST_FIELDS);
        if (!ListQueryUtil.hasSort(form)) query.orderByDesc(ScriptLogEntity::getId);
        else if (!ListQueryUtil.isSortedBy(form, "id")) query.orderByDesc(ScriptLogEntity::getId);
        Page<ScriptLogEntity> page = scriptLogMapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), query);
        List<ScriptLogListVO> records = page.getRecords().stream().map(converter::toLogListVO).toList();
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public ScriptLogDetailVO logDetail(Long id) {
        currentUserContext.checkAdministrator();
        ScriptLogEntity entity = scriptLogMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "脚本执行日志不存在");
        }
        return converter.toLogDetailVO(entity);
    }

    private ScriptExecutionConfig resolveExecutionConfig(String content, String requestedMode) {
        if (content.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "脚本内容不能为空");
        }
        validateSourceLength(content);
        String mode = requestedMode == null ? "ATOMIC" : requestedMode;
        if (!TRANSACTION_MODES.contains(mode)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "脚本事务模式不合法");
        }
        return new ScriptExecutionConfig(mode,
                parameter(TIMEOUT_PARAMETER, 30, 1, 300),
                parameter(MAX_OUTPUT_PARAMETER, 100000, 1000, 1000000));
    }

    private void validateSourceLength(String content) {
        int maxLength = parameter(MAX_SOURCE_PARAMETER, 100000, 1000, 1000000);
        if (content.length() > maxLength) {
            throw new BizException(ResultEnum.PARAM_ERROR, "脚本内容不能超过 " + maxLength + " 个字符");
        }
    }

    private int parameter(String number, int defaultValue, int min, int max) {
        Integer configured = sysParamService.getInt(number);
        int value = configured == null ? defaultValue : configured;
        if (value < min || value > max) {
            throw new BizException(ResultEnum.CONFIG_ERROR,
                    "系统参数 " + number + " 必须在 " + min + "～" + max + " 之间");
        }
        return value;
    }

    private ScriptEntity requireScript(Long id) {
        ScriptEntity entity = scriptMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "脚本不存在");
        }
        return entity;
    }

    private ScriptResultVO toResult(ScriptExecutionOutcome outcome, String mode) {
        ScriptResultVO result = new ScriptResultVO();
        result.setStatus(outcome.status());
        result.setOutput(outcome.output());
        result.setErrorMessage(outcome.errorMessage());
        result.setExecuteDuration(outcome.executeDuration());
        result.setTruncated(outcome.truncated());
        result.setTransactionResult("ATOMIC".equals(mode)
                ? ("SUCCESS".equals(outcome.status()) ? "COMMITTED" : "ROLLED_BACK")
                : "NOT_APPLICABLE");
        return result;
    }

    private ScriptLogEntity createLog(ScriptExecuteForm form, String content, ScriptEntity savedScript,
                                      ScriptResultVO result) {
        ScriptLogEntity log = new ScriptLogEntity();
        log.setScriptId(form.getScriptId());
        log.setScriptName(savedScript == null ? null : savedScript.getName());
        log.setScriptContent(content);
        log.setTransactionMode(form.getTransactionMode() == null ? "ATOMIC" : form.getTransactionMode());
        log.setExecuteStatus(result.getStatus());
        log.setExecuteDuration(result.getExecuteDuration());
        log.setTransactionResult(result.getTransactionResult());
        log.setOutput(result.getOutput());
        log.setErrorMessage(result.getErrorMessage());
        log.setCreateName(currentUserContext.getUsernameOrDefault("未知"));
        try {
            log.setCreateIp(clientIpResolver.resolveCurrentRequest());
        } catch (RuntimeException ignored) {
            log.setCreateIp(null);
        }
        return log;
    }

    private void validateLogForm(ScriptLogListForm form) {
        LogQueryValidator.validateTimeRange(form.getStartTime(), form.getEndTime());
        if (StringUtil.isNotBlank(form.getStatus()) && !STATUSES.contains(form.getStatus())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "执行状态不合法");
        }
        if (StringUtil.isNotBlank(form.getTransactionMode())
                && !TRANSACTION_MODES.contains(form.getTransactionMode())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "事务模式不合法");
        }
    }
}
