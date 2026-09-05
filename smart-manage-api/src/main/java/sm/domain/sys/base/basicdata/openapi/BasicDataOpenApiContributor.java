package sm.domain.sys.base.basicdata.openapi;

import org.springframework.stereotype.Component;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import sm.domain.sys.base.basicdata.openapi.model.form.BasicDataOpenApiQueryForm;
import sm.domain.sys.base.basicdata.openapi.service.BasicDataOpenApiService;
import sm.system.exception.BizException;
import sm.system.openapi.OpenApiOperation;
import sm.system.openapi.OpenApiOperationContributor;
import sm.system.response.ResultEnum;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collection;
import java.util.List;

/** 基础资料模块显式发布的外部 API 操作。 */
@Component
@RequiredArgsConstructor
public class BasicDataOpenApiContributor implements OpenApiOperationContributor {
    public static final String OPERATION_KEY = "sys.basicData.items.queryByCategory";
    public static final String PATH = "/openapi/sys/base/basic-data/v1/items/query";
    private final BasicDataOpenApiService service;
    private final JsonMapper jsonMapper;
    private final Validator validator;

    @Override
    public Collection<OpenApiOperation> operations() {
        return List.of(new OpenApiOperation(OPERATION_KEY, "sys.basic-data.items", "v1",
                "按分类获取基础数据信息", "POST", PATH,
                "sys", "系统管理", "base", "基础平台", "basic-data", "基础资料",
                this::executeTest));
    }

    private Object executeTest(JsonNode request) {
        try {
            BasicDataOpenApiQueryForm form = jsonMapper.treeToValue(request, BasicDataOpenApiQueryForm.class);
            Collection<ConstraintViolation<BasicDataOpenApiQueryForm>> violations = validator.validate(form);
            if (!violations.isEmpty()) {
                throw new BizException(ResultEnum.BAD_REQUEST,
                        violations.iterator().next().getMessage());
            }
            return service.queryByCategory(form.categoryNumber());
        } catch (JacksonException exception) {
            throw new BizException(ResultEnum.BAD_REQUEST, "请求 JSON 与接口参数不匹配");
        }
    }
}
