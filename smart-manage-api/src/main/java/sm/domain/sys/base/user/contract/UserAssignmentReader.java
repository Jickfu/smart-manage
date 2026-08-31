package sm.domain.sys.base.user.contract;

import java.util.Collection;

/** 用户任职关系的最小只读契约。 */
public interface UserAssignmentReader {

    /** 要求用户在指定组织存在有效任职。 */
    void requireAssignment(Long userId, Long orgId);

    /** 是否存在以指定组织为主职的启用用户。 */
    boolean hasEnabledPrimaryAssignments(Collection<Long> orgIds);
}
