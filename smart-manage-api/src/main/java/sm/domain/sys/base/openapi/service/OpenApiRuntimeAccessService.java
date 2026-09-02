package sm.domain.sys.base.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.openapi.mapper.OpenApiApplicationMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiCredentialMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiGrantMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiReleaseMapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiApplicationEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiCredentialEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiGrantEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.contract.UserAssignmentReader;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.system.exception.BizException;
import sm.system.security.crypto.Sm4Cipher;
import sm.system.openapi.OpenApiOperation;
import sm.system.response.ResultEnum;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/** 外部请求读取凭据、发布状态、授权及代理身份的单一安全边界。 */
@Service
@RequiredArgsConstructor
public class OpenApiRuntimeAccessService {
    private final OpenApiCredentialMapper credentialMapper;
    private final OpenApiApplicationMapper applicationMapper;
    private final OpenApiReleaseMapper releaseMapper;
    private final OpenApiGrantMapper grantMapper;
    private final UserReferenceReader userReferenceReader;
    private final UserAssignmentReader userAssignmentReader;
    private final OrgReferenceReader orgReferenceReader;
    private final Sm4Cipher sm4Helper;

    public AccessMaterial authenticate(String keyId, String clientIp) {
        OpenApiCredentialEntity credential = credentialMapper.selectOne(
                new LambdaQueryWrapper<OpenApiCredentialEntity>()
                        .eq(OpenApiCredentialEntity::getKeyId, keyId));
        if (credential == null || !Boolean.TRUE.equals(credential.getEnabled())
                || credential.getExpiresAt() != null && !credential.getExpiresAt().isAfter(LocalDateTime.now())) {
            rejectAuthentication();
        }
        OpenApiApplicationEntity application = applicationMapper.selectById(credential.getApplicationId());
        if (application == null || !Boolean.TRUE.equals(application.getEnabled())) {
            rejectAuthentication();
        }
        if (!java.util.Objects.equals(credential.getEncryptionAlgorithm(), application.getEncryptionAlgorithm())) {
            rejectAuthentication();
        }
        validateIp(application, clientIp);
        String algorithm = switch (application.getEncryptionAlgorithm()) {
            case "NONE" -> "NONE";
            case "AES_256_GCM" -> "AES-256-GCM";
            case "SM4_GCM" -> "SM4-GCM";
            default -> throw new BizException(ResultEnum.CONFIG_ERROR, "OpenAPI 加密算法配置无效");
        };
        return new AccessMaterial(application.getId(), application.getNumber(),
                application.getProxyUserId(), null, application.getProxyOrgId(), algorithm,
                credential.getKeyId(), decryptKey(credential.getSigningSecretCipher()),
                decryptOptionalKey(credential.getRequestEncryptionKeyCipher()),
                decryptOptionalKey(credential.getResponseEncryptionKeyCipher()));
    }

    public AccessMaterial authorizeOperation(AccessMaterial material, OpenApiOperation operation) {
        OpenApiReleaseEntity release = releaseMapper.selectOne(new LambdaQueryWrapper<OpenApiReleaseEntity>()
                .eq(OpenApiReleaseEntity::getOperationKey, operation.operationKey()));
        if (release == null || !"PUBLISHED".equals(release.getStatus())
                || !release.getHttpMethod().equals(operation.httpMethod())
                || !release.getPath().equals(operation.path())) {
            throw new BizException(ResultEnum.NOT_FOUND, "OpenAPI 操作未发布");
        }
        long grants = grantMapper.selectCount(new LambdaQueryWrapper<OpenApiGrantEntity>()
                .eq(OpenApiGrantEntity::getApplicationId, material.applicationId())
                .eq(OpenApiGrantEntity::getOperationKey, operation.operationKey()));
        if (grants == 0) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "第三方应用未获授该 API");
        }
        try {
            UserReference user = userReferenceReader.requireEnabled(material.userId());
            OrgReference org = orgReferenceReader.requireAvailable(material.orgId());
            userAssignmentReader.requireAssignment(user.id(), org.id());
            if ("administrator".equalsIgnoreCase(user.username())) {
                throw new BizException(ResultEnum.PERMISSION_ERROR, "OpenAPI 代理身份当前不可用");
            }
            return new AccessMaterial(material.applicationId(), material.applicationNumber(), user.id(),
                    user.username(), org.id(), material.algorithm(), material.keyId(),
                    material.signingSecret(), material.requestEncryptionKey(), material.responseEncryptionKey());
        } catch (BizException exception) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "OpenAPI 代理身份当前不可用");
        }
    }

    private void validateIp(OpenApiApplicationEntity application, String clientIp) {
        if ("DISABLED".equals(application.getIpPolicyMode())) {
            return;
        }
        List<String> ranges = OpenApiIpRangeMatcher.normalize(application.getIpRanges());
        boolean matches = OpenApiIpRangeMatcher.matches(clientIp, ranges);
        if ("WHITELIST".equals(application.getIpPolicyMode()) && !matches
                || "BLACKLIST".equals(application.getIpPolicyMode()) && matches) {
            rejectAuthentication();
        }
    }

    private byte[] decryptKey(String cipher) {
        try {
            return Base64.getDecoder().decode(sm4Helper.decrypt(cipher));
        } catch (IllegalArgumentException exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "OpenAPI 凭据密钥格式损坏");
        }
    }

    private byte[] decryptOptionalKey(String cipher) {
        return cipher == null ? null : decryptKey(cipher);
    }

    private void rejectAuthentication() {
        throw new BizException(ResultEnum.UNAUTHORIZED, "OpenAPI 请求认证失败");
    }

    public record AccessMaterial(Long applicationId, String applicationNumber,
                                 Long userId, String username, Long orgId,
                                 String algorithm, String keyId,
                                 byte[] signingSecret, byte[] requestEncryptionKey,
                                 byte[] responseEncryptionKey) {
    }
}
