package sm.system.resource;

import org.springframework.stereotype.Component;
import org.apache.tika.Tika;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.web.multipart.MultipartFile;
import java.util.Locale;
import java.io.IOException;

/**
 * 轻量业务资源注册表。只负责稳定类型到授权策略的路由，不反射或推断业务权限。
 */
@Component
public class BusinessResourceRegistry {
    private static final Tika MIME_DETECTOR = new Tika();
    private static final Pattern RESOURCE_TYPE_PATTERN = Pattern.compile(
            "^[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*){2,}$");
    private final Map<String, BusinessResourceAccessPolicy> policyByType;
    private final Map<String, BusinessResourceRegistration> registrationByType;
    private final AttachmentUploadPolicyProvider attachmentUploadPolicyProvider;

    public BusinessResourceRegistry(List<BusinessResourceRegistration> registrations,
                                    AttachmentUploadPolicyProvider attachmentUploadPolicyProvider) {
        this.attachmentUploadPolicyProvider = attachmentUploadPolicyProvider;
        Map<String, BusinessResourceAccessPolicy> registeredPolicies = new HashMap<>();
        Set<String> objectPrefixes = new HashSet<>();
        for (BusinessResourceRegistration registration : registrations) {
            String resourceType = registration.resourceType();
            if (resourceType == null || !RESOURCE_TYPE_PATTERN.matcher(resourceType).matches()) {
                throw new IllegalStateException("业务资源类型必须使用 domain.application.resource 格式: " + resourceType);
            }
            if (registration.accessPolicy() == null) {
                throw new IllegalStateException("业务资源缺少授权策略: " + resourceType);
            }
            if (registeredPolicies.putIfAbsent(resourceType, registration.accessPolicy()) != null) {
                throw new IllegalStateException("业务资源类型重复注册: " + resourceType);
            }
            String objectPrefix = registration.objectPrefix();
            if (objectPrefix == null || !objectPrefix.matches("^(biz|asset)/[a-z0-9/-]+$")
                    || objectPrefix.contains("..") || !objectPrefixes.add(objectPrefix)) {
                throw new IllegalStateException("业务资源对象前缀非法或重复: " + objectPrefix);
            }
        }
        policyByType = Map.copyOf(registeredPolicies);
        Map<String, BusinessResourceRegistration> registeredDefinitions = new HashMap<>();
        for (BusinessResourceRegistration registration : registrations) {
            registeredDefinitions.put(registration.resourceType(), registration);
        }
        registrationByType = Map.copyOf(registeredDefinitions);
    }

    public void requireRegistered(String resourceType) {
        policy(resourceType);
    }

    public void requireAllowed(String resourceType, String resourceId, BusinessResourceAction action) {
        if (resourceId == null || resourceId.isBlank()) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "附件缺少业务资源归属");
        }
        policy(resourceType).requireAllowed(resourceId, action);
    }

    public void validateUpload(String resourceType, MultipartFile file) {
        BusinessResourceRegistration registration = registrationByType.get(resourceType);
        if (registration == null) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "未注册的业务资源类型");
        }
        registration.requireUploadAllowed();
        AttachmentUploadPolicy policy = attachmentUploadPolicyProvider.uploadPolicy();
        if (file == null || file.isEmpty() || file.getSize() > policy.maxUploadBytes()) {
            throw new BizException(ResultEnum.FILE_TOO_LARGE, "上传文件为空或超过全局限制");
        }
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!policy.allowedExtensions().contains(extension)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "文件扩展名不在允许范围内");
        }
        String mimeType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!policy.allowedMimeTypes().contains(mimeType)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "文件 MIME 类型不在允许范围内");
        }
        String detectedMime = detectMime(file);
        if (detectedMime == null || !policy.allowedMimeTypes().contains(detectedMime)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "文件内容与允许的 MIME 类型不匹配");
        }
        if (!detectedMime.equals(mimeType)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "文件扩展名、声明 MIME 与实际内容不一致");
        }
    }

    public String objectPrefix(String resourceType) {
        BusinessResourceRegistration registration = registrationByType.get(resourceType);
        if (registration == null) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "未注册的业务资源类型");
        }
        return registration.objectPrefix();
    }

    public void requireDataScopeAction(String resourceType, String action) {
        BusinessResourceRegistration registration = registrationByType.get(resourceType);
        if (registration == null || !registration.supportsDataScope()
                || action == null || !registration.dataScopeActions().contains(action)) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "业务资源未声明该数据权限操作");
        }
    }

    public Set<String> dataScopeActions(String resourceType) {
        BusinessResourceRegistration registration = registrationByType.get(resourceType);
        if (registration == null || !registration.supportsDataScope()) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "业务资源未启用数据权限");
        }
        return registration.dataScopeActions();
    }

    public Map<String, Set<String>> dataScopeCatalog() {
        Map<String, Set<String>> catalog = new java.util.TreeMap<>();
        for (BusinessResourceRegistration registration : registrationByType.values()) {
            if (registration.supportsDataScope()) {
                catalog.put(registration.resourceType(), Set.copyOf(registration.dataScopeActions()));
            }
        }
        return Map.copyOf(catalog);
    }

    private String detectMime(MultipartFile file) {
        try (java.io.InputStream inputStream = file.getInputStream()) {
            // Tika 同时参考文件内容和原始文件名，可扩展识别类型而无需修改注册表代码。
            return MIME_DETECTOR.detect(inputStream, file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        } catch (IOException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "无法读取上传文件内容");
        }
    }

    private BusinessResourceAccessPolicy policy(String resourceType) {
        BusinessResourceAccessPolicy policy = policyByType.get(resourceType);
        if (policy == null) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "未注册的业务资源类型");
        }
        return policy;
    }
}
