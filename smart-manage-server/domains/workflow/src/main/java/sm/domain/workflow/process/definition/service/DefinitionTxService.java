package sm.domain.workflow.process.definition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.workflow.process.definition.mapper.WorkflowBindingMapper;
import sm.domain.workflow.process.definition.model.entity.WorkflowBindingEntity;
import sm.domain.workflow.process.definition.model.form.*;
import sm.domain.workflow.process.engine.WorkflowDefinitions;
import sm.domain.workflow.process.runtime.service.WorkflowBusinessRegistry;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import java.util.Map;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness.FieldType;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class DefinitionTxService {
    private final WorkflowBindingMapper mapper;
    private final WorkflowDefinitions engine;
    private final WorkflowBusinessRegistry businesses;

    Long create(DefinitionCreateForm form) {
        String businessType = form.businessType();
        businesses.require(businessType);
        var binding = new WorkflowBindingEntity();
        binding.setNumber(form.number());
        binding.setName(form.name());
        binding.setBusinessType(businessType);
        binding.setEnabled(true);
        binding.setVersion(0);
        mapper.insert(binding);
        return engine.create(form.number(), form.name());
    }

    WorkflowDefinitions.Design save(DefinitionSaveForm form) {
        var binding = lockDefinition(form.definitionId());
        engine.save(form.definitionId(), form.definition().toString(), form.digest(), fields(binding));
        return engine.design(form.definitionId());
    }

    void publish(DefinitionPublishForm form) {
        var binding = lockDefinition(form.definitionId());
        engine.publish(form.definitionId(), form.digest(), fields(binding));
    }

    Long copy(Long definitionId) {
        lockDefinition(definitionId);
        return engine.copy(definitionId);
    }

    void enabled(DefinitionEnabledForm form) {
        var binding = mapper.selectById(form.id());
        if (binding == null) throw new BizException(ResultEnum.NOT_FOUND, "流程绑定不存在");
        if (!form.version().equals(binding.getVersion())) throw new BizException(ResultEnum.PARAM_ERROR, "流程设置已变化，请刷新");
        binding.setEnabled(form.enabled());
        if (mapper.updateById(binding) != 1) throw new BizException(ResultEnum.PARAM_ERROR, "流程设置已变化，请刷新");
    }

    private WorkflowBindingEntity lockDefinition(Long id) {
        var definition = engine.describe(id);
        var binding = mapper.selectOne(new LambdaQueryWrapper<WorkflowBindingEntity>()
                .eq(WorkflowBindingEntity::getNumber, definition.code()).last("FOR UPDATE"));
        if (binding == null) throw new BizException(ResultEnum.NOT_FOUND, "流程业务绑定不存在");
        return binding;
    }

    private Map<String, FieldType> fields(WorkflowBindingEntity binding) {
        return binding.getBusinessType() == null ? Map.of() : businesses.require(binding.getBusinessType()).conditionFields();
    }
}
