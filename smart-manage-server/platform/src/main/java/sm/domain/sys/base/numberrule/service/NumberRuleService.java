package sm.domain.sys.base.numberrule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.numberrule.mapper.NumberRuleMapper;
import sm.domain.sys.base.numberrule.mapper.NumberReferenceMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleSegmentMapper;
import sm.domain.sys.base.numberrule.contract.model.NumberReferenceDefinition;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleEntity;
import sm.domain.sys.base.numberrule.model.entity.NumberReferenceEntity;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleSegmentEntity;
import sm.domain.sys.base.numberrule.model.form.NumberRuleDeleteForm;
import sm.domain.sys.base.numberrule.model.form.NumberRuleListForm;
import sm.domain.sys.base.numberrule.model.form.NumberRulePreviewForm;
import sm.domain.sys.base.numberrule.model.form.NumberRuleSaveForm;
import sm.domain.sys.base.numberrule.model.form.NumberRuleSegmentForm;
import sm.domain.sys.base.numberrule.model.vo.NumberReferenceVO;
import sm.domain.sys.base.numberrule.model.vo.NumberRuleOptionVO;
import sm.domain.sys.base.numberrule.model.vo.NumberRuleSegmentVO;
import sm.domain.sys.base.numberrule.model.vo.NumberRuleVO;
import sm.domain.sys.base.numberrule.model.vo.NumberVariableVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListSqlQuery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NumberRuleService {
    private static final String USAGE_COUNT_SQL = "((SELECT count(*) FROM t_sys_number_reference usage_ref "
            + "WHERE usage_ref.default_rule_key = a.rule_key) + (SELECT count(*) FROM t_sys_basic_data_category "
            + "usage_category WHERE usage_category.number_rule_key = a.rule_key "
            + "AND usage_category.number_mode != 'MANUAL'))";
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "ruleKey", ListSqlQuery.string("a.rule_key", true),
            "name", ListSqlQuery.string("a.name", true),
            "featureName", ListSqlQuery.string("COALESCE(c.custom_name, c.default_name)", false),
            "pattern", ListSqlQuery.string("a.pattern", false),
            "scopeType", ListSqlQuery.enumeration("a.scope_type", false),
            "usageCount", ListSqlQuery.number(USAGE_COUNT_SQL, true),
            "defaultRule", ListSqlQuery.bool("(b.default_rule_key = a.rule_key)", false),
            "enabled", ListSqlQuery.bool("a.enabled", false));
    private final NumberRuleMapper mapper;
    private final NumberReferenceMapper referenceMapper;
    private final NumberRuleSegmentMapper segmentMapper;
    private final NumberRuleTxService txService;
    private final NumberReferenceRegistry referenceRegistry;

    public PageData<NumberRuleVO> listPage(NumberRuleListForm form) {
        Page<NumberRuleVO> page = mapper.selectListPage(new Page<>(form.getPageNum(), form.getPageSize()),
                form, ListSqlQuery.of(form, LIST_FIELDS));
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), page.getRecords());
    }

    public NumberRuleVO detail(Long id) {
        if (id == null) throw new BizException(ResultEnum.PARAM_ERROR, "编号规则ID不能为空");
        NumberRuleVO result = mapper.selectDetailById(id);
        if (result == null) throw new BizException(ResultEnum.NOT_FOUND, "编号规则不存在");
        result.setSegments(listSegments(result.getRuleKey()).stream().map(this::toVO).toList());
        return result;
    }

    public List<NumberReferenceVO> references() {
        List<NumberReferenceVO> references = mapper.selectReferences();
        for (NumberReferenceVO reference : references) {
            NumberReferenceDefinition definition = referenceRegistry.require(reference.getReferenceKey());
            if (!definition.featureKey().equals(reference.getFeatureKey())) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "编号引用与功能注册不一致");
            }
            reference.setAllowedScopes(definition.allowedScopes().stream().map(Enum::name)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
            List<sm.domain.sys.base.numberrule.contract.model.NumberVariableDefinition> variables =
                    new ArrayList<>(definition.variables());
            variables.add(NumberRuleBuiltInVariables.SYSTEM_DATE);
            reference.setVariables(variables.stream()
                    .map(variable -> new NumberVariableVO(
                            variable.key(), variable.name(), variable.segmentType().name()))
                    .toList());
        }
        return references;
    }

    public List<NumberRuleOptionVO> options(String scopeType, String referenceKey) {
        LambdaQueryWrapper<NumberRuleEntity> query = new LambdaQueryWrapper<NumberRuleEntity>()
                .eq(NumberRuleEntity::getEnabled, true)
                .eq(scopeType != null && !scopeType.isBlank(), NumberRuleEntity::getScopeType,
                        scopeType == null ? null : scopeType.trim().toUpperCase())
                .eq(referenceKey != null && !referenceKey.isBlank(), NumberRuleEntity::getReferenceKey,
                        referenceKey == null ? null : referenceKey.trim())
                .orderByAsc(NumberRuleEntity::getName)
                .orderByAsc(NumberRuleEntity::getRuleKey);
        String defaultRuleKey = referenceKey == null || referenceKey.isBlank() ? null
                : java.util.Optional.ofNullable(referenceMapper.selectOne(
                        new LambdaQueryWrapper<NumberReferenceEntity>()
                                .eq(NumberReferenceEntity::getReferenceKey, referenceKey.trim())))
                .map(NumberReferenceEntity::getDefaultRuleKey).orElse(null);
        return mapper.selectList(query).stream()
                .map(rule -> new NumberRuleOptionVO(rule.getId(), rule.getRuleKey(), rule.getReferenceKey(),
                        rule.getName(), rule.getScopeType(), rule.getPattern(),
                        rule.getRuleKey().equals(defaultRuleKey)))
                .toList();
    }

    public String preview(NumberRulePreviewForm form) {
        NumberReferenceDefinition definition = referenceRegistry.require(form.getReferenceKey());
        List<NumberRuleSegmentEntity> segments = toSegments(form.getSegments());
        NumberSegmentRenderer.validate(segments, definition);
        return NumberSegmentRenderer.preview(segments, form.getSequenceValue());
    }

    @BizLog("保存编号规则")
    public Long save(NumberRuleSaveForm form) {
        return txService.save(form);
    }

    @BizLog("启用编号规则")
    public void enable(List<Long> ids) {
        txService.updateEnabled(ids, true);
    }

    @BizLog("停用编号规则")
    public void disable(List<Long> ids) {
        txService.updateEnabled(ids, false);
    }

    @BizLog("设置默认编号规则")
    public void setDefault(Long id) {
        txService.setDefault(id);
    }

    @BizLog("删除编号规则")
    public void delete(NumberRuleDeleteForm form) {
        txService.delete(form.getId(), form.getVersion());
    }

    private List<NumberRuleSegmentEntity> listSegments(String ruleKey) {
        return segmentMapper.selectList(new LambdaQueryWrapper<NumberRuleSegmentEntity>()
                .eq(NumberRuleSegmentEntity::getRuleKey, ruleKey)
                .orderByAsc(NumberRuleSegmentEntity::getSort));
    }

    private NumberRuleSegmentVO toVO(NumberRuleSegmentEntity segment) {
        return new NumberRuleSegmentVO(segment.getSort(), segment.getSegmentType(), segment.getValue(),
                segment.getFormat(), segment.getLength(), segment.getSeparator());
    }

    private List<NumberRuleSegmentEntity> toSegments(List<NumberRuleSegmentForm> forms) {
        List<NumberRuleSegmentEntity> segments = new ArrayList<>(forms.size());
        for (NumberRuleSegmentForm form : forms) {
            NumberRuleSegmentEntity segment = new NumberRuleSegmentEntity();
            segment.setSort(form.getSort());
            segment.setSegmentType(form.getSegmentType().trim().toUpperCase());
            segment.setValue(form.getValue() == null ? null : form.getValue().trim());
            segment.setFormat(form.getFormat() == null ? null : form.getFormat().trim());
            segment.setLength(form.getLength());
            segment.setSeparator(form.getSeparator() == null ? "" : form.getSeparator());
            segments.add(segment);
        }
        return segments.stream().sorted(Comparator.comparing(NumberRuleSegmentEntity::getSort)).toList();
    }
}
