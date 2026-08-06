package sm.system.resource;

/**
 * 全局附件上传限制的读取契约。
 * 系统资源注册表仅依赖此抽象，具体配置由系统领域实现，避免基础层反向依赖领域层。
 */
public interface AttachmentUploadPolicyProvider {

    AttachmentUploadPolicy uploadPolicy();
}
