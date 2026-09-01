package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.domain.sys.base.fileartifact.contract.FileArtifactGateway;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.constant.UserExcelSchema;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserExportForm;
import sm.domain.sys.base.user.model.vo.UserAssignmentVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.excel.DataExportLayout;
import sm.system.excel.ExcelWorkbookService;
import sm.system.query.ListSqlQuery;
import sm.system.response.ResultEnum;
import sm.system.security.authorization.AdministratorOnly;
import sm.system.storage.FileStoragePurpose;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 用户 Excel 导出入口，负责导出查询、字段组装和结果制品。 */
@Service
@RequiredArgsConstructor
public class UserExportService {
    private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
            "name", ListSqlQuery.string("a.name", true),
            "number", ListSqlQuery.string("a.number", true),
            "username", ListSqlQuery.string("a.username", true),
            "enabled", ListSqlQuery.bool("a.enabled", true));

    private final UserMapper userMapper;
    private final UserAssignmentMapper userAssignmentMapper;
    private final OrgReferenceReader orgReferenceReader;
    private final ExcelWorkbookService excelWorkbookService;
    private final FileArtifactGateway fileArtifactGateway;

    @BizLog("导出用户")
    @AdministratorOnly
    public FileArtifactReference export(UserExportForm form) {
        List<Long> scopedOrgIds = resolveScopedOrgIds(form);
        Page<UserEntity> result = userMapper.selectScopedPage(new Page<>(1, 10_001),
                form.getKeyword() == null ? null : form.getKeyword().trim(), scopedOrgIds,
                Boolean.TRUE.equals(form.getUnassigned()), form.getIds(), ListSqlQuery.of(form, LIST_FIELDS));
        List<UserEntity> entities = result.getRecords();
        if (result.getTotal() > 10_000) {
            throw new BizException(ResultEnum.PARAM_ERROR, "单次最多导出 10000 个用户");
        }
        Map<Long, List<UserAssignmentVO>> assignments = loadAssignments(
                entities.stream().map(UserEntity::getId).toList());
        List<String> headers = switch (form.getLayout()) {
            case IMPORT_TEMPLATE -> UserExcelSchema.IMPORT_HEADERS;
            case EXPORT_TEMPLATE -> UserExcelSchema.EXPORT_HEADERS;
        };
        List<List<?>> rows = new ArrayList<>();
        for (UserEntity entity : entities) {
            rows.add(exportRow(entity, assignments.getOrDefault(entity.getId(), List.of()), form.getLayout()));
        }
        byte[] content = excelWorkbookService.write("用户", headers, rows);
        return fileArtifactGateway.create(FileStoragePurpose.DATA_EXPORT_RESULT, exportFileName(form.getLayout()),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content,
                Duration.ofHours(24), null);
    }

    private List<Long> resolveScopedOrgIds(UserExportForm form) {
        if (Boolean.TRUE.equals(form.getUnassigned()) || form.getOrgId() == null) return null;
        OrgReference selected = orgReferenceReader.require(form.getOrgId());
        if (!Boolean.TRUE.equals(form.getIncludeDescendants())) return List.of(selected.id());
        String prefix = selected.numberPath() + "/";
        return orgReferenceReader.findAll().stream()
                .filter(org -> org.id().equals(selected.id())
                        || (org.numberPath() != null && org.numberPath().startsWith(prefix)))
                .map(OrgReference::id).toList();
    }

    private Map<Long, List<UserAssignmentVO>> loadAssignments(List<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        List<UserAssignmentEntity> assignments = userAssignmentMapper.selectList(
                new LambdaQueryWrapper<UserAssignmentEntity>()
                        .in(UserAssignmentEntity::getUserId, userIds)
                        .orderByDesc(UserAssignmentEntity::getIsPrimary)
                        .orderByAsc(UserAssignmentEntity::getOrgId));
        Set<Long> orgIds = assignments.stream().map(UserAssignmentEntity::getOrgId).collect(Collectors.toSet());
        Map<Long, OrgReference> orgById = orgReferenceReader.findByIds(orgIds);
        Map<Long, List<UserAssignmentVO>> result = new HashMap<>();
        for (UserAssignmentEntity assignment : assignments) {
            OrgReference org = orgById.get(assignment.getOrgId());
            if (org == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "用户任职关联了无效组织");
            UserAssignmentVO item = new UserAssignmentVO();
            item.setOrg(new ReferenceVO(org.id(), org.number(), org.name()));
            item.setOrgNamePath(org.namePath());
            item.setPosition(assignment.getPosition());
            item.setIsPrimary(assignment.getIsPrimary());
            result.computeIfAbsent(assignment.getUserId(), ignored -> new ArrayList<>()).add(item);
        }
        for (List<UserAssignmentVO> userAssignments : result.values()) {
            userAssignments.sort(Comparator
                    .comparing((UserAssignmentVO assignment) -> !Boolean.TRUE.equals(assignment.getIsPrimary()))
                    .thenComparing(UserAssignmentVO::getOrgNamePath));
        }
        return result;
    }

    private List<?> exportRow(UserEntity entity, List<UserAssignmentVO> assignments, DataExportLayout layout) {
        String orgNames = assignments.stream().map(item -> item.getOrg().getName()).collect(Collectors.joining(";"));
        String positions = assignments.stream().map(UserAssignmentVO::getPosition).collect(Collectors.joining(";"));
        String importAssignments = assignments.stream()
                .map(item -> item.getOrg().getNumber() + ":" + item.getPosition()).collect(Collectors.joining(";"));
        String primaryOrg = assignments.stream().filter(item -> Boolean.TRUE.equals(item.getIsPrimary()))
                .map(item -> item.getOrg().getName()).findFirst().orElse("");
        return switch (layout) {
            case IMPORT_TEMPLATE -> java.util.Arrays.asList(safe(entity.getUsername()), safe(entity.getName()),
                    safe(entity.getNumber()), safe(entity.getEmail()), safe(entity.getPhone()), entity.getGender(),
                    entity.getBirthday(), safe(importAssignments));
            case EXPORT_TEMPLATE -> java.util.Arrays.asList(safe(entity.getUsername()), safe(entity.getName()),
                    safe(entity.getNumber()), safe(entity.getEmail()), safe(entity.getPhone()), entity.getGender(),
                    entity.getBirthday(), Boolean.TRUE.equals(entity.getEnabled()) ? "可用" : "禁用", safe(orgNames),
                    safe(positions), safe(primaryOrg), entity.getCreateTime());
        };
    }

    private String exportFileName(DataExportLayout layout) {
        return switch (layout) {
            case EXPORT_TEMPLATE -> "用户-完整信息.xlsx";
            case IMPORT_TEMPLATE -> "用户-导入模板.xlsx";
        };
    }

    private String safe(String value) {
        return excelWorkbookService.safeText(value == null ? "" : value);
    }
}
