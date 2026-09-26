package sm.domain.workflow.process.definition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;
import sm.domain.workflow.process.definition.mapper.WorkflowBindingMapper;
import sm.domain.workflow.process.definition.model.entity.WorkflowBindingEntity;
import sm.domain.workflow.process.definition.model.form.*;
import sm.domain.workflow.process.definition.model.vo.*;
import sm.domain.workflow.process.engine.WorkflowDefinitions;
import sm.domain.workflow.process.runtime.service.WorkflowBusinessRegistry;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.*;
import java.util.ArrayList;
import java.util.List;
import sm.domain.sys.base.feature.contract.FeatureDirectoryReader;

@Service("workflowDefinitionService")
@RequiredArgsConstructor
public class DefinitionService {
    private final WorkflowBindingMapper mapper;
    private final WorkflowDefinitions engine;
    private final DefinitionTxService transactions;
    private final WorkflowBusinessRegistry businesses;
    private final ObjectMapper json;
    private final FeatureDirectoryReader featureDirectory;

    public PageData<DefinitionListVO> listPage(DefinitionListForm form) {
        var query = new LambdaQueryWrapper<WorkflowBindingEntity>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            var keyword = form.getKeyword().trim();
            query.and(condition -> condition.like(WorkflowBindingEntity::getNumber, keyword)
                    .or().like(WorkflowBindingEntity::getName, keyword));
        }
        if (form.getDomainId() != null || form.getAppId() != null) {
            var businessTypes = directoryChoices().stream()
                    .filter(choice -> form.getDomainId() == null || form.getDomainId().equals(choice.domainId()))
                    .filter(choice -> form.getAppId() == null || form.getAppId().equals(choice.appId()))
                    .map(WorkflowBusinessChoiceVO::key).toList();
            // 空范围必须返回空页，不能让 MyBatis-Plus 忽略空 IN 后退化为全量查询。
            if (businessTypes.isEmpty()) query.apply("FALSE");
            else query.in(WorkflowBindingEntity::getBusinessType, businessTypes);
        }
        var page = mapper.selectPage(Page.of(form.getPageNum(), form.getPageSize()),
                query.orderByDesc(WorkflowBindingEntity::getId));
        var records = new ArrayList<DefinitionListVO>();
        for (var binding : page.getRecords()) records.add(new DefinitionListVO(binding.getId(), binding.getNumber(),
                binding.getName(), binding.getBusinessType(), Boolean.TRUE.equals(binding.getEnabled()), binding.getVersion(), engine.list(binding.getNumber())));
        return PageData.of(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }
    public DefinitionDesignVO design(Long id) {
        var design = engine.design(id);
        return new DefinitionDesignVO(json.readTree(design.json()), design.digest());
    }
    public DefinitionListVO versions(Long id) {
        var binding = mapper.selectById(id);
        if (binding == null) throw new BizException(ResultEnum.NOT_FOUND, "流程定义不存在");
        return new DefinitionListVO(binding.getId(), binding.getNumber(), binding.getName(),
                binding.getBusinessType(), Boolean.TRUE.equals(binding.getEnabled()), binding.getVersion(), engine.list(binding.getNumber()));
    }
    public List<WorkflowBusinessChoiceVO> businessTypes() {
        return directoryChoices();
    }

    private List<WorkflowBusinessChoiceVO> directoryChoices() {
        var choices = businesses.choices();
        var directory = featureDirectory.findByFeatureKeys(choices.stream().map(WorkflowBusinessRegistry.Choice::featureKey).toList());
        var knownKeys = directory.stream().map(entry -> entry.featureKey()).collect(java.util.stream.Collectors.toSet());
        for (var choice : choices) {
            if (!knownKeys.contains(choice.featureKey())) throw new BizException(ResultEnum.PARAM_ERROR, "审批业务缺少有效功能目录关联：" + choice.key());
        }
        // 目录决定排序与归属，接入注册决定可选能力；不能从功能目录制造未实现的审批业务。
        return directory.stream().flatMap(entry -> choices.stream()
                .filter(choice -> choice.featureKey().equals(entry.featureKey()))
                .map(choice -> new WorkflowBusinessChoiceVO(choice.key(), choice.name(), choice.featureKey(),
                        entry.domainId(), entry.domainName(), entry.appId(), entry.appName()))).toList();
    }

    @BizLog("创建流程定义")
    public Long create(DefinitionCreateForm form) { return transactions.create(form); }
    @BizLog(value = "保存流程设计", recordRequest = false)
    public DefinitionDesignVO save(DefinitionSaveForm form) {
        var saved = transactions.save(form);
        return new DefinitionDesignVO(json.readTree(saved.json()), saved.digest());
    }
    @BizLog("发布流程定义")
    public void publish(DefinitionPublishForm form) { transactions.publish(form); }
    @BizLog("复制流程版本")
    public Long copy(Long id) { return transactions.copy(id); }
    @BizLog("设置流程发起开关")
    public void enabled(DefinitionEnabledForm form) { transactions.enabled(form); }

    /** 调用方已持有业务主单锁；绑定锁将停用、发布和发起串行化。 */
    public String requireFlowForSubmission(String businessType) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("发起流程必须位于业务事务内");
        businesses.require(businessType);
        var binding = mapper.selectOne(new LambdaQueryWrapper<WorkflowBindingEntity>()
                .eq(WorkflowBindingEntity::getBusinessType, businessType).last("FOR UPDATE"));
        if (binding == null || !Boolean.TRUE.equals(binding.getEnabled())) throw new BizException(ResultEnum.PARAM_ERROR, "此业务尚未配置可发起的流程");
        return binding.getNumber();
    }
}
