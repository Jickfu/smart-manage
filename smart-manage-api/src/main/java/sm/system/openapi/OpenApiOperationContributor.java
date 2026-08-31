package sm.system.openapi;

import java.util.Collection;

/** API 提供模块通过该扩展点显式贡献操作，禁止扫描 Controller 自动推断业务 API。 */
public interface OpenApiOperationContributor {
    Collection<OpenApiOperation> operations();
}
