package sm.domain.sys.base.org.contract;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** 组织引用读取与有效性校验契约。 */
public interface OrgReferenceReader {

    OrgReference require(Long orgId);

    OrgReference requireAvailable(Long orgId);

    Map<Long, OrgReference> findByIds(Collection<Long> orgIds);

    List<OrgReference> findAll();
}
