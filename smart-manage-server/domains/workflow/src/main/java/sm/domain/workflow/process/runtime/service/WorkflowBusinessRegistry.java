package sm.domain.workflow.process.runtime.service;

import org.springframework.stereotype.Component;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 由已装配业务注册接入能力，不在工作流中枚举 demo 等可选领域。 */
@Component
public class WorkflowBusinessRegistry {
    private final Map<String, WorkflowBusiness> businesses = new LinkedHashMap<>();
    public WorkflowBusinessRegistry(List<WorkflowBusiness> providers) {
        for (var provider : providers) {
            if (provider.featureKey() == null || provider.featureKey().isBlank()) throw new IllegalStateException("工作流业务必须声明功能目录关联：" + provider.key());
            if (businesses.putIfAbsent(provider.key(), provider) != null) throw new IllegalStateException("重复工作流业务类型：" + provider.key());
        }
    }
    public WorkflowBusiness require(String key) {
        var business = businesses.get(key);
        if (business == null) throw new BizException(ResultEnum.PARAM_ERROR, "当前发行包未装配此审批业务");
        return business;
    }
    public List<Choice> choices() { return businesses.values().stream().map(business -> new Choice(business.key(), business.name(), business.featureKey())).toList(); }
    public record Choice(String key, String name, String featureKey) { }
}
