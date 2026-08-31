package sm.domain.sys.base.org.contract;

/** 组织模块发布的最小只读引用。 */
public record OrgReference(Long id, Long parentId, String number, String name, String numberPath, String namePath,
                           String orgType,
                           boolean enabled, boolean archived) {
}
