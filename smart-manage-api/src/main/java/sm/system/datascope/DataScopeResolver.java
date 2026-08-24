package sm.system.datascope;

/** 跨业务领域解析当前操作者数据范围的稳定系统内核契约。 */
public interface DataScopeResolver {

    DataScope resolve(String resourceType, String action);
}
