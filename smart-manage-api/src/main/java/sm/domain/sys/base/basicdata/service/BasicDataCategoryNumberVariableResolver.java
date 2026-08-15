package sm.domain.sys.base.basicdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.basicdata.mapper.BasicDataCategoryMapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataCategoryEntity;
import sm.domain.sys.base.numberrule.model.NumberGenerationContext;
import sm.domain.sys.base.numberrule.service.NumberVariableResolver;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Component
@RequiredArgsConstructor
public class BasicDataCategoryNumberVariableResolver implements NumberVariableResolver {
    private final BasicDataCategoryMapper mapper;

    @Override
    public String variableKey() {
        return "category.number";
    }

    @Override
    public String resolve(NumberGenerationContext context) {
        if (context == null || context.categoryId() == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "编号上下文缺少基础资料分类ID");
        }
        BasicDataCategoryEntity category = mapper.selectById(context.categoryId());
        if (category == null) throw new BizException(ResultEnum.NOT_FOUND, "基础资料分类不存在");
        return category.getNumber();
    }
}
