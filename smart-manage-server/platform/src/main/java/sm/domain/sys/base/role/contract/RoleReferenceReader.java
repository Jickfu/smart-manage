package sm.domain.sys.base.role.contract;
import sm.system.response.PageData;
import java.util.Collection;
import java.util.Map;
/** 角色选择和流程配置回显需要的最小引用，不暴露角色授权配置。 */
public interface RoleReferenceReader {
    PageData<RoleReference> search(String keyword, int pageNum, int pageSize);
    Map<Long, RoleReference> findByIds(Collection<Long> ids);
}
