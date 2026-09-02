package sm.domain.sys.base.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.openapi.mapper.OpenApiApplicationMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiCredentialMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiGrantMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiReleaseMapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiApplicationEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiCredentialEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiGrantEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
import sm.domain.sys.base.openapi.model.form.OpenApiApplicationSaveForm;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.contract.UserAssignmentReader;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.system.exception.BizException;
import sm.system.security.crypto.Sm4Cipher;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class OpenApiTxService {
    private final OpenApiApplicationMapper applicationMapper;
    private final OpenApiCredentialMapper credentialMapper;
    private final OpenApiGrantMapper grantMapper;
    private final OpenApiReleaseMapper releaseMapper;
    private final UserReferenceReader userReferenceReader;
    private final UserAssignmentReader userAssignmentReader;
    private final OrgReferenceReader orgReferenceReader;
    private final Sm4Cipher sm4Helper;
    private final CurrentUserContext currentUserContext;

    Long saveApplication(OpenApiApplicationSaveForm form) {
        validatePolicy(form);
        validateProxyIdentity(form.proxyUserId(), form.proxyOrgId());
        OpenApiApplicationEntity entity = form.id() == null
                ? new OpenApiApplicationEntity() : requireApplication(form.id());
        if (form.id() != null && !Objects.equals(entity.getVersion(), form.version())) {
            conflict();
        }
        if (form.id() != null
                && !Objects.equals(entity.getEncryptionAlgorithm(), form.encryptionAlgorithm())) {
            long enabledCredentials = credentialMapper.selectCount(
                    new LambdaQueryWrapper<OpenApiCredentialEntity>()
                            .eq(OpenApiCredentialEntity::getApplicationId, form.id())
                            .eq(OpenApiCredentialEntity::getEnabled, true));
            if (enabledCredentials > 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT,
                        "切换报文加密算法前必须先停用全部旧凭据，切换后再创建新凭据");
            }
        }
        long duplicates = applicationMapper.selectCount(new LambdaQueryWrapper<OpenApiApplicationEntity>()
                .eq(OpenApiApplicationEntity::getNumber, form.number().trim())
                .ne(form.id() != null, OpenApiApplicationEntity::getId, form.id()));
        if (duplicates > 0) {
            throw new BizException(ResultEnum.UNIQUE_CONFLICT, "第三方应用编码已存在");
        }
        entity.setNumber(form.number().trim());
        entity.setName(form.name().trim());
        entity.setProxyUserId(form.proxyUserId());
        entity.setProxyOrgId(form.proxyOrgId());
        entity.setAuthenticationType(form.authenticationType());
        entity.setEncryptionAlgorithm(form.encryptionAlgorithm());
        entity.setIpPolicyMode(form.ipPolicyMode());
        List<String> ranges = OpenApiIpRangeMatcher.normalize(form.ipRanges());
        entity.setIpRanges(ranges.isEmpty() ? null : String.join("\n", ranges));
        entity.setDescription(trim(form.description()));
        if (form.id() == null) {
            entity.setEnabled(false);
        }
        int changed = form.id() == null ? applicationMapper.insert(entity) : applicationMapper.updateById(entity);
        if (changed != 1) {
            conflict();
        }
        replaceGrants(entity.getId(), form.operationKeys());
        return entity.getId();
    }

    void enableApplication(Long id, Integer version, boolean enabled) {
        OpenApiApplicationEntity entity = requireApplication(id);
        if (!Objects.equals(entity.getVersion(), version)) {
            conflict();
        }
        if (enabled) {
            validateProxyIdentity(entity.getProxyUserId(), entity.getProxyOrgId());
            long activeCredentials = credentialMapper.selectCount(new LambdaQueryWrapper<OpenApiCredentialEntity>()
                    .eq(OpenApiCredentialEntity::getApplicationId, id)
                    .eq(OpenApiCredentialEntity::getEnabled, true)
                    .eq(OpenApiCredentialEntity::getEncryptionAlgorithm,
                            entity.getEncryptionAlgorithm())
                    .and(wrapper -> wrapper.isNull(OpenApiCredentialEntity::getExpiresAt)
                            .or().gt(OpenApiCredentialEntity::getExpiresAt, LocalDateTime.now())));
            if (activeCredentials == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "请先创建至少一个有效凭据包");
            }
        }
        entity.setEnabled(enabled);
        if (applicationMapper.updateById(entity) != 1) {
            conflict();
        }
    }

    OpenApiCredentialEntity createCredential(Long applicationId, String keyId, String name,
                                               LocalDateTime expiresAt, byte[] signingSecret,
                                               byte[] requestKey, byte[] responseKey) {
        OpenApiApplicationEntity application = requireApplication(applicationId);
        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "凭据过期时间必须晚于当前时间");
        }
        OpenApiCredentialEntity entity = new OpenApiCredentialEntity();
        entity.setApplicationId(applicationId);
        entity.setKeyId(keyId);
        entity.setName(name.trim());
        entity.setEnabled(true);
        entity.setEncryptionAlgorithm(application.getEncryptionAlgorithm());
        entity.setSigningSecretCipher(sm4Helper.encrypt(java.util.Base64.getEncoder().encodeToString(signingSecret)));
        entity.setRequestEncryptionKeyCipher(encryptKey(requestKey));
        entity.setResponseEncryptionKeyCipher(encryptKey(responseKey));
        entity.setExpiresAt(expiresAt);
        if (credentialMapper.insert(entity) != 1) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "创建 OpenAPI 凭据失败");
        }
        return entity;
    }

    void enableCredential(Long id, Integer version, boolean enabled) {
        OpenApiCredentialEntity entity = requireCredential(id);
        if (!Objects.equals(entity.getVersion(), version)) {
            conflict();
        }
        if (enabled && entity.getExpiresAt() != null && !entity.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "已过期凭据不能重新启用");
        }
        OpenApiApplicationEntity application = requireApplication(entity.getApplicationId());
        if (enabled && !Objects.equals(entity.getEncryptionAlgorithm(), application.getEncryptionAlgorithm())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "凭据加密算法与应用当前策略不一致，请创建新凭据");
        }
        entity.setEnabled(enabled);
        if (credentialMapper.updateById(entity) != 1) {
            conflict();
        }
    }

    private void replaceGrants(Long applicationId, List<String> operationKeys) {
        Set<String> distinctKeys = new HashSet<>(operationKeys);
        if (distinctKeys.size() != operationKeys.size()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "API 授权中存在重复操作");
        }
        if (!distinctKeys.isEmpty()) {
            List<OpenApiReleaseEntity> releases = releaseMapper.selectList(
                    new LambdaQueryWrapper<OpenApiReleaseEntity>()
                            .in(OpenApiReleaseEntity::getOperationKey, distinctKeys));
            if (releases.size() != distinctKeys.size()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "API 授权包含未注册操作");
            }
        }
        grantMapper.delete(new LambdaQueryWrapper<OpenApiGrantEntity>()
                .eq(OpenApiGrantEntity::getApplicationId, applicationId));
        for (String operationKey : distinctKeys) {
            OpenApiGrantEntity grant = new OpenApiGrantEntity();
            grant.setApplicationId(applicationId);
            grant.setOperationKey(operationKey);
            if (grantMapper.insert(grant) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "保存 API 授权失败");
            }
        }
    }

    void updateReleaseStatus(Long id, Integer version, String status) {
        if (!Set.of("PUBLISHED", "OFFLINE").contains(status)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "API 版本状态不受支持");
        }
        OpenApiReleaseEntity release = releaseMapper.selectById(id);
        if (release == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "API 版本不存在");
        }
        if (!Objects.equals(release.getVersion(), version)) {
            conflict();
        }
        if (releaseMapper.updateStatus(id, version, status, currentUserContext.getUserId()) != 1) {
            conflict();
        }
    }

    OpenApiApplicationEntity requireApplication(Long id) {
        OpenApiApplicationEntity entity = id == null ? null : applicationMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "第三方应用不存在");
        }
        return entity;
    }

    OpenApiCredentialEntity requireCredential(Long id) {
        OpenApiCredentialEntity entity = id == null ? null : credentialMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "OpenAPI 凭据不存在");
        }
        return entity;
    }

    private void validateProxyIdentity(Long userId, Long orgId) {
        UserReference user = userReferenceReader.requireEnabled(userId);
        if ("administrator".equalsIgnoreCase(user.username())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "超级管理员不能作为 OpenAPI 代理用户");
        }
        orgReferenceReader.requireAvailable(orgId);
        userAssignmentReader.requireAssignment(userId, orgId);
    }

    private void validatePolicy(OpenApiApplicationSaveForm form) {
        if (!"HMAC_SHA256".equals(form.authenticationType())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "一期仅支持 HMAC-SHA256 认证");
        }
        if (!Set.of("NONE", "AES_256_GCM", "SM4_GCM").contains(form.encryptionAlgorithm())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "报文加密算法不受支持");
        }
        if (!Set.of("DISABLED", "WHITELIST", "BLACKLIST").contains(form.ipPolicyMode())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "IP 访问策略不受支持");
        }
        List<String> ranges = OpenApiIpRangeMatcher.normalize(form.ipRanges());
        if (!"DISABLED".equals(form.ipPolicyMode()) && ranges.isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "启用 IP 黑白名单时至少配置一个地址或网段");
        }
    }

    private static String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String encryptKey(byte[] key) {
        return key == null ? null
                : sm4Helper.encrypt(java.util.Base64.getEncoder().encodeToString(key));
    }

    private static void conflict() {
        throw new BizException(ResultEnum.DATA_CONFLICT, "数据已被其他用户修改，请刷新后重试");
    }
}
