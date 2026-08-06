package sm.system.resource;

import java.util.List;

/** 附件模块执行校验时使用的全局上传限制。 */
public record AttachmentUploadPolicy(long maxUploadBytes, List<String> allowedExtensions,
                                     List<String> allowedMimeTypes, int tempExpireHours) {
}
