package sm.domain.sys.base.org.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.numberrule.model.NumberGenerationContext;
import sm.domain.sys.base.numberrule.service.NumberVariableResolver;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Component
@RequiredArgsConstructor
public class OrgNumberVariableResolver implements NumberVariableResolver {
    private final OrgMapper mapper;

    @Override
    public String variableKey() {
        return "org.number";
    }

    @Override
    public String resolve(NumberGenerationContext context) {
        if (context == null || context.orgId() == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号上下文缺少组织ID");
        }
        OrgEntity organization = mapper.selectById(context.orgId());
        if (organization == null) throw new BizException(ResultEnum.NOT_FOUND, "组织不存在");
        return organization.getNumber();
    }
}
