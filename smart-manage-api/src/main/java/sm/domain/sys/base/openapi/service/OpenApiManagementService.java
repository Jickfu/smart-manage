package sm.domain.sys.base.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.openapi.mapper.OpenApiApplicationMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiCredentialMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiGrantMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiInvocationLogMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiReleaseMapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiApplicationEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiCredentialEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiGrantEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiInvocationLogEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
import sm.domain.sys.base.openapi.model.form.OpenApiApplicationSaveForm;
import sm.domain.sys.base.openapi.model.form.OpenApiCredentialCreateForm;
import sm.domain.sys.base.openapi.model.form.OpenApiListForm;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.openapi.OpenApiOperation;
import sm.system.openapi.OpenApiOperationRegistry;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** OpenAPI 平台管理端统一入口。 */
@Service
@RequiredArgsConstructor
public class OpenApiManagementService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final OpenApiApplicationMapper applicationMapper;
    private final OpenApiCredentialMapper credentialMapper;
    private final OpenApiGrantMapper grantMapper;
    private final OpenApiReleaseMapper releaseMapper;
    private final OpenApiInvocationLogMapper invocationLogMapper;
    private final UserReferenceReader userReferenceReader;
    private final OrgReferenceReader orgReferenceReader;
    private final OpenApiOperationRegistry operationRegistry;
    private final OpenApiTxService txService;

    public PageData<Map<String, Object>> applicationList(OpenApiListForm form) {
        LambdaQueryWrapper<OpenApiApplicationEntity> wrapper = new LambdaQueryWrapper<>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            wrapper.and(query -> query.like(OpenApiApplicationEntity::getNumber, keyword)
                    .or().like(OpenApiApplicationEntity::getName, keyword));
        }
        wrapper.eq(form.getEnabled() != null, OpenApiApplicationEntity::getEnabled, form.getEnabled())
                .orderByAsc(OpenApiApplicationEntity::getNumber);
        Page<OpenApiApplicationEntity> page = applicationMapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), wrapper);
        List<Map<String, Object>> records = new ArrayList<>();
        for (OpenApiApplicationEntity entity : page.getRecords()) {
            records.add(applicationMap(entity, false));
        }
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public Map<String, Object> applicationDetail(Long id) {
        OpenApiApplicationEntity entity = txService.requireApplication(id);
        Map<String, Object> result = applicationMap(entity, true);
        List<String> grants = grantMapper.selectList(new LambdaQueryWrapper<OpenApiGrantEntity>()
                        .eq(OpenApiGrantEntity::getApplicationId, id)
                        .orderByAsc(OpenApiGrantEntity::getOperationKey))
                .stream().map(OpenApiGrantEntity::getOperationKey).toList();
        result.put("operationKeys", grants);
        result.put("credentials", credentialList(id));
        return result;
    }

    @BizLog("保存第三方应用")
    public Long saveApplication(OpenApiApplicationSaveForm form) {
        return txService.saveApplication(form);
    }

    @BizLog("启停第三方应用")
    public void enableApplication(Long id, Integer version, boolean enabled) {
        txService.enableApplication(id, version, enabled);
    }

    public List<Map<String, Object>> credentialList(Long applicationId) {
        txService.requireApplication(applicationId);
        return credentialMapper.selectList(new LambdaQueryWrapper<OpenApiCredentialEntity>()
                        .eq(OpenApiCredentialEntity::getApplicationId, applicationId)
                        .orderByDesc(OpenApiCredentialEntity::getCreateTime))
                .stream().map(this::credentialMap).toList();
    }

    @BizLog("创建 OpenAPI 凭据")
    public Map<String, Object> createCredential(OpenApiCredentialCreateForm form) {
        byte[] signingSecret = randomBytes(32);
        OpenApiApplicationEntity application = txService.requireApplication(form.applicationId());
        int encryptionKeyLength = switch (application.getEncryptionAlgorithm()) {
            case "AES_256_GCM" -> 32;
            case "SM4_GCM" -> 16;
            case "NONE" -> 0;
            default -> throw new BizException(ResultEnum.CONFIG_ERROR, "OpenAPI 加密算法配置无效");
        };
        byte[] requestKey = encryptionKeyLength == 0 ? null : randomBytes(encryptionKeyLength);
        byte[] responseKey = encryptionKeyLength == 0 ? null : randomBytes(encryptionKeyLength);
        String keyId = "sm_" + compactRandom(18);
        OpenApiCredentialEntity credential = txService.createCredential(form.applicationId(), keyId,
                form.name(), form.expiresAt(), signingSecret, requestKey, responseKey);
        Map<String, Object> result = credentialMap(credential);
        result.put("signingSecret", Base64.getEncoder().encodeToString(signingSecret));
        if (requestKey != null) {
            result.put("requestEncryptionKey", Base64.getEncoder().encodeToString(requestKey));
            result.put("responseEncryptionKey", Base64.getEncoder().encodeToString(responseKey));
        }
        result.put("oneTimeVisible", true);
        return result;
    }

    @BizLog("启停 OpenAPI 凭据")
    public void enableCredential(Long id, Integer version, boolean enabled) {
        txService.enableCredential(id, version, enabled);
    }

    public PageData<Map<String, Object>> catalogList(OpenApiListForm form) {
        LambdaQueryWrapper<OpenApiReleaseEntity> wrapper = new LambdaQueryWrapper<>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            wrapper.and(query -> query.like(OpenApiReleaseEntity::getApiNumber, keyword)
                    .or().like(OpenApiReleaseEntity::getName, keyword)
                    .or().like(OpenApiReleaseEntity::getOperationKey, keyword));
        }
        wrapper.eq(form.getDomainKey() != null && !form.getDomainKey().isBlank(),
                        OpenApiReleaseEntity::getDomainKey, form.getDomainKey())
                .eq(form.getApplicationKey() != null && !form.getApplicationKey().isBlank(),
                        OpenApiReleaseEntity::getApplicationKey, form.getApplicationKey())
                .eq(form.getFeatureKey() != null && !form.getFeatureKey().isBlank(),
                        OpenApiReleaseEntity::getFeatureKey, form.getFeatureKey());
        wrapper.orderByAsc(OpenApiReleaseEntity::getApiNumber)
                .orderByDesc(OpenApiReleaseEntity::getApiVersion);
        Page<OpenApiReleaseEntity> page = releaseMapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), wrapper);
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(),
                page.getRecords().stream().map(this::releaseMap).toList());
    }

    public List<Map<String, Object>> catalogHierarchy() {
        List<OpenApiReleaseEntity> releases = releaseMapper.selectList(
                new LambdaQueryWrapper<OpenApiReleaseEntity>()
                        .select(OpenApiReleaseEntity::getDomainKey, OpenApiReleaseEntity::getDomainName,
                                OpenApiReleaseEntity::getApplicationKey, OpenApiReleaseEntity::getApplicationName,
                                OpenApiReleaseEntity::getFeatureKey, OpenApiReleaseEntity::getFeatureName)
                        .orderByAsc(OpenApiReleaseEntity::getDomainKey)
                        .orderByAsc(OpenApiReleaseEntity::getApplicationKey)
                        .orderByAsc(OpenApiReleaseEntity::getFeatureKey));
        Map<String, Map<String, Object>> nodes = new LinkedHashMap<>();
        for (OpenApiReleaseEntity release : releases) {
            String domainNodeKey = "domain:" + release.getDomainKey();
            nodes.putIfAbsent(domainNodeKey, map("key", domainNodeKey, "parentKey", null,
                    "type", "DOMAIN", "title", release.getDomainName()));
            String applicationNodeKey = "application:" + release.getDomainKey()
                    + "/" + release.getApplicationKey();
            nodes.putIfAbsent(applicationNodeKey, map("key", applicationNodeKey,
                    "parentKey", domainNodeKey, "type", "APPLICATION",
                    "title", release.getApplicationName()));
            String featureNodeKey = "feature:" + release.getDomainKey() + "/"
                    + release.getApplicationKey() + "/" + release.getFeatureKey();
            nodes.putIfAbsent(featureNodeKey, map("key", featureNodeKey,
                    "parentKey", applicationNodeKey, "type", "FEATURE",
                    "title", release.getFeatureName()));
        }
        return List.copyOf(nodes.values());
    }

    public Map<String, Object> catalogDetail(Long id) {
        OpenApiReleaseEntity release = releaseMapper.selectById(id);
        if (release == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "API 版本不存在");
        }
        return releaseMap(release);
    }

    @BizLog("变更 API 版本状态")
    public void updateReleaseStatus(Long id, Integer version, String status) {
        txService.updateReleaseStatus(id, version, status);
    }

    public PageData<Map<String, Object>> invocationList(OpenApiListForm form) {
        LambdaQueryWrapper<OpenApiInvocationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(form.getApplicationId() != null, OpenApiInvocationLogEntity::getApplicationId,
                        form.getApplicationId())
                .eq(form.getOperationKey() != null && !form.getOperationKey().isBlank(),
                        OpenApiInvocationLogEntity::getOperationKey, form.getOperationKey())
                .eq(form.getResultType() != null && !form.getResultType().isBlank(),
                        OpenApiInvocationLogEntity::getResultType, form.getResultType())
                .orderByDesc(OpenApiInvocationLogEntity::getRequestTime)
                .orderByDesc(OpenApiInvocationLogEntity::getId);
        Page<OpenApiInvocationLogEntity> page = invocationLogMapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), wrapper);
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(),
                page.getRecords().stream().map(this::invocationMap).toList());
    }

    public Map<String, Object> invocationStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", invocationLogMapper.selectLast24HoursSummary());
        result.put("operations", invocationLogMapper.selectLast24HoursByOperation());
        return result;
    }

    private Map<String, Object> applicationMap(OpenApiApplicationEntity entity, boolean detail) {
        Map<String, Object> result = map("id", entity.getId(), "number", entity.getNumber(),
                "name", entity.getName(), "enabled", entity.getEnabled(),
                "proxyUserId", entity.getProxyUserId(), "proxyOrgId", entity.getProxyOrgId(),
                "authenticationType", entity.getAuthenticationType(),
                "encryptionAlgorithm", entity.getEncryptionAlgorithm(),
                "ipPolicyMode", entity.getIpPolicyMode(), "description", entity.getDescription(),
                "version", entity.getVersion(), "createTime", entity.getCreateTime(),
                "updateTime", entity.getUpdateTime());
        if (detail) {
            result.put("ipRanges", entity.getIpRanges());
            UserReference user = userReferenceReader.require(entity.getProxyUserId());
            OrgReference org = orgReferenceReader.require(entity.getProxyOrgId());
            result.put("proxyUser", map("id", user.id(), "number", user.number(),
                    "name", user.name(), "username", user.username()));
            result.put("proxyOrg", map("id", org.id(), "number", org.number(),
                    "name", org.name(), "namePath", org.namePath()));
        }
        return result;
    }

    private Map<String, Object> credentialMap(OpenApiCredentialEntity entity) {
        return map("id", entity.getId(), "applicationId", entity.getApplicationId(),
                "keyId", entity.getKeyId(), "name", entity.getName(), "enabled", entity.getEnabled(),
                "encryptionAlgorithm", entity.getEncryptionAlgorithm(),
                "expiresAt", entity.getExpiresAt(), "lastUsedAt", entity.getLastUsedAt(),
                "createTime", entity.getCreateTime(), "version", entity.getVersion());
    }

    private Map<String, Object> releaseMap(OpenApiReleaseEntity entity) {
        OpenApiOperation registered = operationRegistry.all().stream()
                .filter(operation -> operation.operationKey().equals(entity.getOperationKey()))
                .findFirst().orElse(null);
        return map("id", entity.getId(), "apiNumber", entity.getApiNumber(),
                "apiVersion", entity.getApiVersion(), "operationKey", entity.getOperationKey(),
                "name", entity.getName(), "httpMethod", entity.getHttpMethod(), "path", entity.getPath(),
                "domainKey", entity.getDomainKey(), "domainName", entity.getDomainName(),
                "applicationKey", entity.getApplicationKey(), "applicationName", entity.getApplicationName(),
                "featureKey", entity.getFeatureKey(), "featureName", entity.getFeatureName(),
                "status", entity.getStatus(), "description", entity.getDescription(),
                "requestSchema", entity.getRequestSchema(), "responseSchema", entity.getResponseSchema(),
                "documentation", entity.getDocumentation(), "systemPreset", entity.getSystemPreset(),
                "registered", registered != null, "version", entity.getVersion(),
                "updateTime", entity.getUpdateTime());
    }

    private Map<String, Object> invocationMap(OpenApiInvocationLogEntity entity) {
        return map("id", entity.getId(), "requestTime", entity.getRequestTime(),
                "applicationId", entity.getApplicationId(), "applicationNumber", entity.getApplicationNumber(),
                "credentialKeyId", entity.getCredentialKeyId(), "operationKey", entity.getOperationKey(),
                "requestId", entity.getRequestId(), "traceId", entity.getTraceId(),
                "clientIp", entity.getClientIp(), "resultType", entity.getResultType(),
                "resultCode", entity.getResultCode(), "durationMs", entity.getDurationMs(),
                "requestBytes", entity.getRequestBytes(), "responseBytes", entity.getResponseBytes(),
                "errorMessage", entity.getErrorMessage());
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return bytes;
    }

    private String compactRandom(int bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(bytes));
    }

    private static Map<String, Object> map(Object... entries) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], entries[index + 1]);
        }
        return result;
    }
}
