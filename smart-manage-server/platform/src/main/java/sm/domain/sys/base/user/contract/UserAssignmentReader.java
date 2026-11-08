package sm.domain.sys.base.user.contract;

import java.util.Collection;
import java.util.List;

/** 用户任职关系的最小只读契约。 */
public interface UserAssignmentReader {

    /** 要求用户在指定组织存在有效任职。 */
    void requireAssignment(Long userId, Long orgId);

    /** 是否存在以指定组织为主职的启用用户。 */
    boolean hasEnabledPrimaryAssignments(Collection<Long> orgIds);

    /** 返回指定启用组织中仍启用的负责人，兼任也属于有效任职。 */
    List<Long> findEnabledOrgLeaders(Long orgId);

    /** 返回在指定组织显式分配该角色的启用任职人员。默认角色的隐式继承只用于平台授权。 */
    List<Long> findEnabledExplicitRoleMembers(Long orgId, Long roleId);
}
