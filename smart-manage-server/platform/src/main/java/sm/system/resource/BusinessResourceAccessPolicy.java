package sm.system.resource;

/**
 * 业务资源授权策略。具体业务模块负责校验资源存在性、当前状态和用户权限。
 */
public interface BusinessResourceAccessPolicy {
    void requireAllowed(String resourceId, BusinessResourceAction action);

    /** 存在历史快照的业务可按具体附件授权；普通资源仍继承单据授权。 */
    default void requireAttachmentAllowed(String resourceId, Long attachmentId, BusinessResourceAction action) {
        requireAllowed(resourceId, action);
    }

    /** 在附件写事务内执行的业务约束，可锁定主单以防快照冻结与删除竞争。 */
    default void beforeAttachmentMutation(String resourceId, Long attachmentId, BusinessResourceAction action) {
    }
}
