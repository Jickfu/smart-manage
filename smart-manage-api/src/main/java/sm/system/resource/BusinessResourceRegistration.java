package sm.system.resource;

import java.util.Set;

/** 业务模块向系统公共能力显式注册的稳定资源类型。 */
public interface BusinessResourceRegistration {
    String resourceType();

    BusinessResourceAccessPolicy accessPolicy();

    /** 对象键稳定前缀，不接受前端目录输入。 */
    default String objectPrefix() {
        return "biz/" + resourceType().replace('.', '/');
    }

    /** 上传入口权限属于业务语义；文件大小、扩展名和 MIME 由附件全局配置统一限制。 */
    default void requireUploadAllowed() {
    }

    /** 只有业务资源显式声明后，角色默认范围才会参与该资源的行级授权。 */
    default boolean supportsDataScope() {
        return false;
    }

    /** 数据操作是领域访问语义，不从功能权限码或按钮名称推断。 */
    default Set<String> dataScopeActions() {
        return Set.of();
    }
}
