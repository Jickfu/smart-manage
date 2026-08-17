package sm.domain.sys.base.numberrule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.base.numberrule.mapper.NumberReferenceMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleSegmentMapper;
import sm.domain.sys.base.numberrule.model.NumberGenerationContext;
import sm.domain.sys.base.numberrule.model.NumberReferenceDefinition;
import sm.domain.sys.base.numberrule.model.NumberResetPeriod;
import sm.domain.sys.base.numberrule.model.NumberScopeType;
import sm.domain.sys.base.numberrule.model.entity.NumberReferenceEntity;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleEntity;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleSegmentEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** 业务模块正式取号入口。业务只引用稳定 referenceKey，不接触表名或字段名。 */
@Component
@RequiredArgsConstructor
public class NumberGeneratorAccessor {
    private static final String GLOBAL_SCOPE_KEY = "GLOBAL";
    private static final String NEVER_PERIOD_KEY = "NEVER";

    private final NumberRuleMapper ruleMapper;
    private final NumberReferenceMapper referenceMapper;
    private final NumberRuleSegmentMapper segmentMapper;
    private final NumberReferenceRegistry referenceRegistry;
    private final NumberVariableResolverRegistry variableResolvers;

    public String nextNumber(String referenceKey, NumberGenerationContext context) {
        NumberReferenceEntity reference = requireReference(referenceKey);
        if (reference.getDefaultRuleKey() == null || reference.getDefaultRuleKey().isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号引用未配置默认规则");
        }
        return nextNumber(referenceKey, reference.getDefaultRuleKey(), context);
    }

    /** 基础资料分类等业务拥有者可以在所属编号引用内选择具体规则。 */
    public String nextNumber(String referenceKey, String ruleKey, NumberGenerationContext context) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "正式取号必须在业务事务中执行");
        }
        NumberReferenceDefinition definition = referenceRegistry.require(referenceKey);
        NumberRuleEntity rule = requireEnabledRule(referenceKey, ruleKey);
        List<NumberRuleSegmentEntity> segments = listSegments(rule.getRuleKey());
        validateRule(rule, segments, definition);
        NumberGenerationContext safeContext = context == null
                ? new NumberGenerationContext(null, null, null) : context;
        ResolvedSegment counterSegment = resolveCounterSegment(rule, safeContext, segments);
        Long sequenceValue = ruleMapper.nextValue(rule.getRuleKey(), counterSegment.scopeKey(),
                counterSegment.periodKey(), rule.getStartValue());
        if (sequenceValue == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "编号流水生成失败");
        return NumberSegmentRenderer.render(segments, sequenceValue, safeContext, variableResolvers);
    }

    public void validateRule(String referenceKey, String ruleKey, NumberScopeType expectedScope,
                             boolean requireEnabled) {
        NumberReferenceDefinition definition = referenceRegistry.require(referenceKey);
        NumberRuleEntity rule = requireRule(referenceKey, ruleKey);
        if (requireEnabled && !Boolean.TRUE.equals(rule.getEnabled())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号规则已停用");
        }
        if (expectedScope != null && !expectedScope.name().equals(rule.getScopeType())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号规则流水作用域必须为" + expectedScope.name());
        }
        validateRule(rule, listSegments(ruleKey), definition);
    }

    private NumberReferenceEntity requireReference(String referenceKey) {
        if (referenceKey == null || referenceKey.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号引用不能为空");
        }
        NumberReferenceEntity reference = referenceMapper.selectOne(new LambdaQueryWrapper<NumberReferenceEntity>()
                .eq(NumberReferenceEntity::getReferenceKey, referenceKey.trim()));
        if (reference == null) throw new BizException(ResultEnum.NOT_FOUND, "编号引用不存在");
        referenceRegistry.require(reference.getReferenceKey());
        return reference;
    }

    private NumberRuleEntity requireEnabledRule(String referenceKey, String ruleKey) {
        NumberRuleEntity rule = requireRule(referenceKey, ruleKey);
        if (!Boolean.TRUE.equals(rule.getEnabled())) throw new BizException(ResultEnum.PARAM_ERROR, "编号规则已停用");
        return rule;
    }

    private NumberRuleEntity requireRule(String referenceKey, String ruleKey) {
        if (ruleKey == null || ruleKey.isBlank()) throw new BizException(ResultEnum.PARAM_ERROR, "编号规则键不能为空");
        NumberRuleEntity rule = ruleMapper.selectOne(new LambdaQueryWrapper<NumberRuleEntity>()
                .eq(NumberRuleEntity::getRuleKey, ruleKey.trim()));
        if (rule == null) throw new BizException(ResultEnum.NOT_FOUND, "编号规则不存在");
        if (!referenceKey.equals(rule.getReferenceKey())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号规则不属于当前编号引用");
        }
        return rule;
    }

    private List<NumberRuleSegmentEntity> listSegments(String ruleKey) {
        return segmentMapper.selectList(new LambdaQueryWrapper<NumberRuleSegmentEntity>()
                .eq(NumberRuleSegmentEntity::getRuleKey, ruleKey)
                .orderByAsc(NumberRuleSegmentEntity::getSort));
    }

    private void validateRule(NumberRuleEntity rule, List<NumberRuleSegmentEntity> segments,
                              NumberReferenceDefinition definition) {
        NumberScopeType scopeType = parseScopeType(rule.getScopeType());
        if (!definition.allowedScopes().contains(scopeType)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号引用不支持该流水作用域");
        }
        NumberSegmentRenderer.validate(segments, definition);
    }

    private ResolvedSegment resolveCounterSegment(NumberRuleEntity rule, NumberGenerationContext context,
                                                   List<NumberRuleSegmentEntity> segments) {
        NumberScopeType scopeType = parseScopeType(rule.getScopeType());
        String scopeKey = switch (scopeType) {
            case GLOBAL -> GLOBAL_SCOPE_KEY;
            case ORG -> requirePositiveId(context.orgId(), "组织ID");
            case CATEGORY -> requirePositiveId(context.categoryId(), "基础资料分类ID");
        };
        NumberResetPeriod resetPeriod = parseResetPeriod(rule.getResetPeriod());
        LocalDate businessDate = context.businessDate();
        boolean hasBusinessDateSegment = segments.stream().anyMatch(segment ->
                "DATE".equals(segment.getSegmentType())
                        && !NumberRuleBuiltInVariables.SYSTEM_DATE_KEY.equals(segment.getValue()));
        boolean hasSystemDateSegment = segments.stream().anyMatch(segment ->
                "DATE".equals(segment.getSegmentType())
                        && NumberRuleBuiltInVariables.SYSTEM_DATE_KEY.equals(segment.getValue()));
        if (hasBusinessDateSegment && businessDate == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号规则需要业务日期");
        }
        LocalDate periodDate = businessDate;
        if (resetPeriod != NumberResetPeriod.NEVER && periodDate == null) {
            if (!hasSystemDateSegment) {
                throw new BizException(ResultEnum.PARAM_ERROR, "编号规则需要业务日期");
            }
            periodDate = LocalDate.now();
        }
        String periodKey = switch (resetPeriod) {
            case NEVER -> NEVER_PERIOD_KEY;
            case YEAR -> periodDate.format(DateTimeFormatter.ofPattern("yyyy"));
            case MONTH -> periodDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
            case DAY -> periodDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        };
        return new ResolvedSegment(scopeKey, periodKey);
    }

    private String requirePositiveId(Long value, String label) {
        if (value == null || value <= 0) throw new BizException(ResultEnum.PARAM_ERROR, label + "不能为空");
        return String.valueOf(value);
    }

    private NumberScopeType parseScopeType(String value) {
        try {
            return NumberScopeType.valueOf(value);
        } catch (RuntimeException exception) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "编号规则流水作用域无效");
        }
    }

    private NumberResetPeriod parseResetPeriod(String value) {
        try {
            return NumberResetPeriod.valueOf(value);
        } catch (RuntimeException exception) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "编号规则重置周期无效");
        }
    }

    private record ResolvedSegment(String scopeKey, String periodKey) {
    }
}
