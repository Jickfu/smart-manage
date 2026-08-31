package sm.domain.sys.base.basicdata.openapi;

import org.springframework.stereotype.Component;
import sm.system.openapi.OpenApiOperation;
import sm.system.openapi.OpenApiOperationContributor;

import java.util.Collection;
import java.util.List;

/** 基础资料模块显式发布的外部 API 操作。 */
@Component
public class BasicDataOpenApiContributor implements OpenApiOperationContributor {
    public static final String OPERATION_KEY = "sys.basicData.items.queryByCategory";
    public static final String PATH = "/openapi/sys/base/basic-data/v1/items/query";

    @Override
    public Collection<OpenApiOperation> operations() {
        return List.of(new OpenApiOperation(OPERATION_KEY, "sys.basic-data.items", "v1",
                "按分类获取基础数据信息", "POST", PATH,
                "sys", "系统管理", "base", "基础平台", "basic-data", "基础资料"));
    }
}
