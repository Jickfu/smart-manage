package sm.domain.sys.base.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.openapi.mapper.OpenApiApplicationMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiGrantMapper;
import sm.domain.sys.base.openapi.mapper.OpenApiReleaseMapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiApplicationEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiGrantEntity;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;
import sm.domain.sys.base.openapi.model.form.OpenApiCatalogTestForm;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.openapi.OpenApiActorContext;
import sm.system.openapi.OpenApiOperation;
import sm.system.openapi.OpenApiOperationRegistry;
import sm.system.response.ResultEnum;
import sm.system.security.authorization.AdministratorOnly;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 仅真实 administrator 可进入的 OpenAPI 业务试调边界。 */
@Service
@AdministratorOnly
@RequiredArgsConstructor
public class OpenApiCatalogTestService {
    private final OpenApiReleaseMapper releaseMapper;
    private final OpenApiApplicationMapper applicationMapper;
    private final OpenApiGrantMapper grantMapper;
    private final OpenApiOperationRegistry operationRegistry;
    private final OpenApiRuntimeAccessService runtimeAccessService;
    private final JsonMapper jsonMapper;

    public List<Map<String, Object>> availableApplications(Long releaseId) {
        OpenApiReleaseEntity release = requireTestableRelease(releaseId);
        List<Long> applicationIds = grantMapper.selectList(new LambdaQueryWrapper<OpenApiGrantEntity>()
                        .eq(OpenApiGrantEntity::getOperationKey, release.getOperationKey()))
                .stream().map(OpenApiGrantEntity::getApplicationId).distinct().toList();
        if (applicationIds.isEmpty()) return List.of();
        return applicationMapper.selectList(new LambdaQueryWrapper<OpenApiApplicationEntity>()
                        .in(OpenApiApplicationEntity::getId, applicationIds)
                        .eq(OpenApiApplicationEntity::getEnabled, true)
                        .orderByAsc(OpenApiApplicationEntity::getNumber))
                .stream().map(application -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", application.getId());
                    item.put("number", application.getNumber());
                    item.put("name", application.getName());
                    return item;
                }).toList();
    }

    @BizLog(value = "OpenAPI 业务试调", recordRequest = false, recordResponse = false)
    public Map<String, Object> execute(OpenApiCatalogTestForm form) {
        OpenApiReleaseEntity release = requireTestableRelease(form.releaseId());
        OpenApiOperation operation = operationRegistry.findByKey(release.getOperationKey());
        OpenApiRuntimeAccessService.AccessMaterial material =
                runtimeAccessService.authorizeManagementTest(form.applicationId(), operation);
        JsonNode request;
        try {
            request = jsonMapper.readTree(form.requestJson());
        } catch (JacksonException exception) {
            throw new BizException(ResultEnum.BAD_REQUEST, "请求数据必须是有效 JSON");
        }
        if (request == null || !request.isObject()) {
            throw new BizException(ResultEnum.BAD_REQUEST, "请求数据必须是 JSON 对象");
        }
        long startedAt = System.nanoTime();
        String requestId = "test_" + UUID.randomUUID();
        Object response;
        try (OpenApiActorContext.Scope ignored = OpenApiActorContext.open(material.applicationId(),
                material.applicationNumber(), material.userId(), material.username(), material.orgId(), requestId)) {
            response = operation.testHandler().execute(request);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requestId", requestId);
        result.put("durationMs", (System.nanoTime() - startedAt) / 1_000_000);
        result.put("response", response);
        return result;
    }

    private OpenApiReleaseEntity requireTestableRelease(Long releaseId) {
        OpenApiReleaseEntity release = releaseMapper.selectById(releaseId);
        if (release == null) throw new BizException(ResultEnum.NOT_FOUND, "API 版本不存在");
        OpenApiOperation operation = operationRegistry.findByKey(release.getOperationKey());
        if (operation == null || !operation.testable()) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "该 API 未开放管理端业务试调");
        }
        if (!"PUBLISHED".equals(release.getStatus())) {
            throw new BizException(ResultEnum.NOT_FOUND, "API 版本尚未发布");
        }
        return release;
    }
}
