package sm.system.datascope;

import java.util.Set;

/** 多角色 Allow 规则合并后的不可变数据范围。SELF 的字段语义由业务领域解释。 */
public record DataScope(boolean all, boolean selfIncluded, Set<Long> orgIds, Long currentUserId) {
    public DataScope {
        orgIds = Set.copyOf(orgIds);
    }
}
