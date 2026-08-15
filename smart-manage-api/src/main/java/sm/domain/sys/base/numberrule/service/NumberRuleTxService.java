package sm.domain.sys.base.numberrule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.numberrule.mapper.NumberReferenceMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleMapper;
import sm.domain.sys.base.numberrule.mapper.NumberRuleSegmentMapper;
import sm.domain.sys.base.numberrule.model.NumberReferenceDefinition;
import sm.domain.sys.base.numberrule.model.NumberResetPeriod;
import sm.domain.sys.base.numberrule.model.NumberScopeType;
import sm.domain.sys.base.numberrule.model.entity.NumberReferenceEntity;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleEntity;
import sm.domain.sys.base.numberrule.model.entity.NumberRuleSegmentEntity;
import sm.domain.sys.base.numberrule.model.form.NumberRuleSaveForm;
import sm.domain.sys.base.numberrule.model.form.NumberRuleSegmentForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class NumberRuleTxService {
    private final NumberRuleMapper mapper;
    private final NumberReferenceMapper referenceMapper;
    private final NumberRuleSegmentMapper segmentMapper;
    private final NumberReferenceRegistry referenceRegistry;

    public Long save(NumberRuleSaveForm form) {
        normalize(form);
        NumberReferenceEntity reference = requireReference(form.getReferenceKey());
        NumberReferenceDefinition definition = referenceRegistry.require(reference.getReferenceKey());
        List<NumberRuleSegmentEntity> segments = toSegments(form.getRuleKey(), form.getSegments());
        validateConfiguration(form, segments, definition);

        NumberRuleEntity entity;
        if (form.getId() == null) {
            entity = new NumberRuleEntity();
            entity.setSystemPreset(false);
            entity.setEnabled(true);
        } else {
            entity = requireRule(form.getId());
            requireVersion(entity, form.getVersion());
            if (!entity.getRuleKey().equals(form.getRuleKey())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "编号规则键是稳定身份，创建后不能修改");
            }
            if (!entity.getReferenceKey().equals(form.getReferenceKey())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "编号规则创建后不能更换编号引用");
            }
            if (Boolean.TRUE.equals(entity.getSystemPreset()) && !entity.getScopeType().equals(form.getScopeType())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "系统预置规则不能修改流水作用域");
            }
        }
        long duplicates = mapper.selectCount(new LambdaQueryWrapper<NumberRuleEntity>()
                .eq(NumberRuleEntity::getRuleKey, form.getRuleKey())
                .ne(form.getId() != null, NumberRuleEntity::getId, form.getId()));
        if (duplicates > 0) throw new BizException(ResultEnum.DATA_CONFLICT, "编号规则键已存在");

        entity.setRuleKey(form.getRuleKey());
        entity.setReferenceKey(form.getReferenceKey());
        entity.setName(form.getName());
        String compiledPattern = NumberSegmentRenderer.compilePattern(segments);
        if (compiledPattern.length() > 200) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号格式定义不能超过200个字符");
        }
        entity.setPattern(compiledPattern);
        entity.setScopeType(form.getScopeType());
        entity.setResetPeriod(form.getResetPeriod());
        entity.setStartValue(form.getStartValue().longValue());
        entity.setDescription(form.getDescription());
        int affected = form.getId() == null ? mapper.insert(entity) : mapper.updateById(entity);
        if (affected != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "编号规则已被其他用户修改");

        segmentMapper.delete(new LambdaQueryWrapper<NumberRuleSegmentEntity>()
                .eq(NumberRuleSegmentEntity::getRuleKey, entity.getRuleKey()));
        for (NumberRuleSegmentEntity segment : segments) {
            segment.setRuleKey(entity.getRuleKey());
            if (segmentMapper.insert(segment) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "编号格式段保存失败");
            }
        }
        return entity.getId();
    }

    public void updateEnabled(List<Long> ids, boolean enabled) {
        if (ids == null || ids.isEmpty()) throw new BizException(ResultEnum.PARAM_ERROR, "编号规则ID不能为空");
        List<NumberRuleEntity> rules = mapper.selectBatchIds(ids.stream().distinct().toList());
        if (rules.size() != ids.stream().distinct().count()) {
            throw new BizException(ResultEnum.NOT_FOUND, "部分编号规则不存在");
        }
        for (NumberRuleEntity rule : rules) {
            if (!enabled && countActiveUsages(rule.getRuleKey()) > 0) {
                throw new BizException(ResultEnum.PARAM_ERROR,
                        "编号规则仍被默认规则或自动编号分类引用，请先更换引用：" + rule.getName());
            }
            if (Objects.equals(rule.getEnabled(), enabled)) continue;
            rule.setEnabled(enabled);
            if (mapper.updateById(rule) != 1) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "编号规则已被其他用户修改");
            }
        }
    }

    public void setDefault(Long ruleId) {
        NumberRuleEntity rule = requireRule(ruleId);
        if (!Boolean.TRUE.equals(rule.getEnabled())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "停用的编号规则不能设为默认规则");
        }
        NumberReferenceEntity reference = requireReference(rule.getReferenceKey());
        if (Objects.equals(reference.getDefaultRuleKey(), rule.getRuleKey())) return;
        reference.setDefaultRuleKey(rule.getRuleKey());
        if (referenceMapper.updateById(reference) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "编号引用已被其他用户修改");
        }
    }

    public void delete(Long id, Integer version) {
        NumberRuleEntity entity = requireRule(id);
        requireVersion(entity, version);
        if (Boolean.TRUE.equals(entity.getSystemPreset())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "系统预置编号规则不能删除");
        }
        if (countActiveUsages(entity.getRuleKey()) > 0 || mapper.countCounters(entity.getRuleKey()) > 0) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号规则已被引用或已产生流水，不能删除");
        }
        segmentMapper.delete(new LambdaQueryWrapper<NumberRuleSegmentEntity>()
                .eq(NumberRuleSegmentEntity::getRuleKey, entity.getRuleKey()));
        int affected = mapper.delete(new LambdaQueryWrapper<NumberRuleEntity>()
                .eq(NumberRuleEntity::getId, id)
                .eq(NumberRuleEntity::getVersion, version));
        if (affected != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "编号规则已被其他用户修改");
    }

    private long countActiveUsages(String ruleKey) {
        return mapper.countDefaultReferences(ruleKey) + mapper.countAutomaticCategoryReferences(ruleKey);
    }

    private void normalize(NumberRuleSaveForm form) {
        form.setRuleKey(form.getRuleKey().trim());
        form.setReferenceKey(form.getReferenceKey().trim());
        form.setName(form.getName().trim());
        form.setScopeType(form.getScopeType().trim().toUpperCase());
        form.setResetPeriod(form.getResetPeriod().trim().toUpperCase());
        form.setDescription(form.getDescription() == null || form.getDescription().isBlank()
                ? null : form.getDescription().trim());
        try {
            NumberScopeType.valueOf(form.getScopeType());
            NumberResetPeriod.valueOf(form.getResetPeriod());
        } catch (IllegalArgumentException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流水作用域或重置周期无效");
        }
    }

    private void validateConfiguration(NumberRuleSaveForm form, List<NumberRuleSegmentEntity> segments,
                                       NumberReferenceDefinition definition) {
        NumberScopeType scopeType = NumberScopeType.valueOf(form.getScopeType());
        if (!definition.allowedScopes().contains(scopeType)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号引用不支持该流水作用域");
        }
        NumberSegmentRenderer.validate(segments, definition);
    }

    private List<NumberRuleSegmentEntity> toSegments(String ruleKey, List<NumberRuleSegmentForm> forms) {
        List<NumberRuleSegmentEntity> segments = new ArrayList<>(forms.size());
        for (NumberRuleSegmentForm form : forms) {
            NumberRuleSegmentEntity segment = new NumberRuleSegmentEntity();
            segment.setRuleKey(ruleKey);
            segment.setSort(form.getSort());
            segment.setSegmentType(form.getSegmentType().trim().toUpperCase());
            segment.setValue(form.getValue() == null ? null : form.getValue().trim());
            segment.setFormat(form.getFormat() == null ? null : form.getFormat().trim());
            segment.setLength(form.getLength());
            segment.setSeparator(form.getSeparator() == null ? "" : form.getSeparator());
            segments.add(segment);
        }
        return segments;
    }

    private NumberReferenceEntity requireReference(String referenceKey) {
        NumberReferenceEntity reference = referenceMapper.selectOne(new LambdaQueryWrapper<NumberReferenceEntity>()
                .eq(NumberReferenceEntity::getReferenceKey, referenceKey));
        if (reference == null) throw new BizException(ResultEnum.NOT_FOUND, "编号引用不存在");
        return reference;
    }

    private NumberRuleEntity requireRule(Long id) {
        if (id == null) throw new BizException(ResultEnum.PARAM_ERROR, "编号规则ID不能为空");
        NumberRuleEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "编号规则不存在");
        return entity;
    }

    private void requireVersion(NumberRuleEntity entity, Integer version) {
        if (version == null) throw new BizException(ResultEnum.PARAM_ERROR, "乐观锁版本号不能为空");
        if (!Objects.equals(entity.getVersion(), version)) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "编号规则已被其他用户修改");
        }
    }
}
