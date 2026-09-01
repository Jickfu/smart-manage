package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.domain.sys.base.fileartifact.service.FileArtifactService;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.constant.UserExcelSchema;
import sm.domain.sys.base.user.model.Gender;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserAssignmentForm;
import sm.domain.sys.base.user.model.enums.UserImportMode;
import sm.domain.sys.base.user.model.enums.UserImportTransactionMode;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.vo.UserImportResultVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.excel.ExcelWorkbookService;
import sm.system.response.ResultEnum;
import sm.system.security.authorization.AdministratorOnly;
import sm.system.auth.SessionTerminationReason;
import sm.system.storage.FileStoragePurpose;
import sm.system.util.PasswordGeneratorUtil;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/** 用户 Excel 导入入口；凭据只写入一次性制品，绝不进入业务日志参数。 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserImportService {
    private static final List<String> HEADERS = UserExcelSchema.IMPORT_HEADERS;
    private final ExcelWorkbookService excelWorkbookService;
    private final UserMapper userMapper;
    private final OrgReferenceReader orgReferenceReader;
    private final UserImportTxService importTxService;
    private final FileArtifactService fileArtifactService;
    private final Validator validator;
    private final AuthorizationStateHelper authorizationStateHelper;

    @AdministratorOnly
    public byte[] template() {
        return excelWorkbookService.write("用户", HEADERS, List.of());
    }

    @BizLog(value = "导入用户", recordRequest = false)
    @AdministratorOnly
    public UserImportResultVO importUsers(MultipartFile file, UserImportMode mode,
                                          UserImportTransactionMode transactionMode) {
        byte[] content = readAndValidateFile(file);
        fileArtifactService.create(FileStoragePurpose.DATA_IMPORT_SOURCE,
                file.getOriginalFilename() == null ? "用户导入.xlsx" : file.getOriginalFilename(),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                content, Duration.ofHours(24), null);
        List<Map<String, String>> rows = excelWorkbookService.read(content, HEADERS);
        ImportPlan plan = buildPlan(rows, mode);
        if (transactionMode == UserImportTransactionMode.ATOMIC && !plan.errors().isEmpty()) {
            return new UserImportResultVO(rows.size(), 0, rows.size(), plan.errors(), List.of(), List.of(),
                    createErrorFile(plan.errors()));
        }

        List<FileArtifactReference> credentialFiles = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int success = 0;
        List<String> errors = new ArrayList<>(plan.errors());
        if (transactionMode == UserImportTransactionMode.ATOMIC) {
            BatchExecution execution = commitBatch(plan.rows(), "用户初始密码.xlsx");
            credentialFiles.addAll(execution.credentialFiles());
            warnings.addAll(execution.warnings());
            success = plan.rows().size();
        } else {
            final int batchSize = 200;
            for (int start = 0; start < plan.rows().size(); start += batchSize) {
                int end = Math.min(start + batchSize, plan.rows().size());
                List<PlannedUserImportRow> batchRows = plan.rows().subList(start, end);
                String rowLabel = formatRowNumbers(batchRows);
                try {
                    String fileName = credentialFileName(batchRows);
                    BatchExecution execution = commitBatch(batchRows, fileName);
                    credentialFiles.addAll(execution.credentialFiles());
                    warnings.addAll(execution.warnings());
                    success += end - start;
                } catch (RuntimeException exception) {
                    errors.add(rowLabel + "写入失败：" + exception.getMessage());
                }
            }
        }
        return new UserImportResultVO(rows.size(), success, rows.size() - success, errors, warnings, credentialFiles,
                createErrorFile(errors));
    }

    private ImportPlan buildPlan(List<Map<String, String>> rows, UserImportMode mode) {
        Map<String, UserEntity> existingByUsername = new HashMap<>();
        Set<String> importedUsernames = new HashSet<>();
        for (Map<String, String> row : rows) {
            String username = row.getOrDefault(HEADERS.get(0), "").trim();
            if (!username.isBlank()) importedUsernames.add(username);
        }
        if (!importedUsernames.isEmpty()) {
            for (UserEntity entity : userMapper.selectList(new LambdaQueryWrapper<UserEntity>()
                    .in(UserEntity::getUsername, importedUsernames))) {
                existingByUsername.put(entity.getUsername(), entity);
            }
        }
        Map<String, OrgReference> orgByNumber = new HashMap<>();
        for (OrgReference org : orgReferenceReader.findAll()) orgByNumber.put(org.number(), org);
        List<PlannedUserImportRow> plannedRows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> usernames = new HashSet<>();
        Map<String, Integer> numberRows = new HashMap<>();
        Map<String, Integer> emailRows = new HashMap<>();
        Map<String, Integer> phoneRows = new HashMap<>();
        for (int index = 0; index < rows.size(); index++) {
            int rowNumber = index + 2;
            Map<String, String> row = rows.get(index);
            String username = row.getOrDefault(HEADERS.get(0), "").trim();
            String name = row.getOrDefault(HEADERS.get(1), "").trim();
            if (username.isBlank() || name.isBlank()) { errors.add("第 " + rowNumber + " 行：登录账号和姓名不能为空"); continue; }
            if (!usernames.add(username)) { errors.add("第 " + rowNumber + " 行：登录账号在文件内重复"); continue; }
            UserEntity existing = existingByUsername.get(username);
            if (mode == UserImportMode.CREATE_ONLY && existing != null) { errors.add("第 " + rowNumber + " 行：登录账号已存在"); continue; }
            if (mode == UserImportMode.UPDATE_ONLY && existing == null) { errors.add("第 " + rowNumber + " 行：登录账号不存在"); continue; }
            UserSaveForm form = new UserSaveForm();
            form.setUsername(username);
            form.setName(name);
            String number = row.getOrDefault(HEADERS.get(2), "").trim();
            form.setNumber(number.isBlank() ? existing == null ? username : existing.getNumber() : number);
            form.setEmail(emptyToNull(row.get(HEADERS.get(3))));
            form.setPhone(emptyToNull(row.get(HEADERS.get(4))));
            String normalizedEmail = normalizeEmail(form.getEmail());
            String normalizedPhone = emptyToNull(form.getPhone());
            form.setEmail(normalizedEmail);
            form.setPhone(normalizedPhone);
            try {
                String gender = row.getOrDefault(HEADERS.get(5), "");
                form.setGender(gender.isBlank() ? null : Gender.valueOf(gender));
                String birthday = row.getOrDefault(HEADERS.get(6), "");
                form.setBirthday(birthday.isBlank() ? null : LocalDate.parse(birthday));
                form.setAssignments(assignments(row.get(HEADERS.get(7)), orgByNumber));
            } catch (RuntimeException exception) { errors.add("第 " + rowNumber + " 行：" + exception.getMessage()); continue; }
            Credential credential = null;
            if (existing == null) {
                String password = PasswordGeneratorUtil.generate(12);
                form.setPassword(password);
                credential = new Credential(username, name, password);
            } else {
                form.setId(existing.getId());
                form.setVersion(existing.getVersion());
            }
            Set<ConstraintViolation<UserSaveForm>> violations = validator.validate(form);
            if (!violations.isEmpty()) {
                String message = violations.stream().map(ConstraintViolation::getMessage).sorted()
                        .collect(java.util.stream.Collectors.joining("；"));
                errors.add("第 " + rowNumber + " 行：" + message);
                continue;
            }
            boolean duplicate = reportDuplicateBusinessKey(numberRows, form.getNumber(), rowNumber, "工号", errors)
                    | reportDuplicateBusinessKey(emailRows, normalizedEmail, rowNumber, "邮箱", errors)
                    | reportDuplicateBusinessKey(phoneRows, normalizedPhone, rowNumber, "手机号", errors);
            if (duplicate) {
                continue;
            }
            numberRows.put(form.getNumber(), rowNumber);
            if (normalizedEmail != null) emailRows.put(normalizedEmail, rowNumber);
            if (normalizedPhone != null) phoneRows.put(normalizedPhone, rowNumber);
            plannedRows.add(new PlannedUserImportRow(rowNumber, form, credential));
        }
        return new ImportPlan(removeDatabaseConflicts(plannedRows, errors), errors);
    }

    private List<PlannedUserImportRow> removeDatabaseConflicts(List<PlannedUserImportRow> plannedRows,
                                                                List<String> errors) {
        if (plannedRows.isEmpty()) return plannedRows;
        Set<String> numbers = new HashSet<>();
        Set<String> emails = new HashSet<>();
        Set<String> phones = new HashSet<>();
        for (PlannedUserImportRow plannedRow : plannedRows) {
            UserSaveForm form = plannedRow.form();
            numbers.add(form.getNumber());
            if (form.getEmail() != null) emails.add(form.getEmail());
            if (form.getPhone() != null) phones.add(form.getPhone());
        }
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<>();
        query.and(wrapper -> {
            wrapper.in(UserEntity::getNumber, numbers);
            if (!emails.isEmpty()) wrapper.or().in(UserEntity::getEmail, emails);
            if (!phones.isEmpty()) wrapper.or().in(UserEntity::getPhone, phones);
        });
        List<UserEntity> conflicts = userMapper.selectList(query);
        List<PlannedUserImportRow> accepted = new ArrayList<>();
        for (PlannedUserImportRow plannedRow : plannedRows) {
            UserSaveForm form = plannedRow.form();
            String conflictField = null;
            for (UserEntity entity : conflicts) {
                if (java.util.Objects.equals(entity.getId(), form.getId())) continue;
                if (form.getNumber().equals(entity.getNumber())) conflictField = "工号";
                else if (form.getEmail() != null && form.getEmail().equals(normalizeEmail(entity.getEmail()))) conflictField = "邮箱";
                else if (form.getPhone() != null && form.getPhone().equals(emptyToNull(entity.getPhone()))) conflictField = "手机号";
                if (conflictField != null) break;
            }
            if (conflictField == null) accepted.add(plannedRow);
            else errors.add("第 " + plannedRow.sourceRowNumber() + " 行：" + conflictField + "已存在");
        }
        return accepted;
    }

    private boolean reportDuplicateBusinessKey(Map<String, Integer> firstRows, String value, int rowNumber,
                                               String fieldName, List<String> errors) {
        if (value == null) return false;
        Integer firstRow = firstRows.get(value);
        if (firstRow == null) return false;
        errors.add("第 " + rowNumber + " 行：" + fieldName + "与第 " + firstRow + " 行重复");
        return true;
    }

    private List<UserAssignmentForm> assignments(String value, Map<String, OrgReference> orgByNumber) {
        if (value == null || value.isBlank()) return List.of();
        List<UserAssignmentForm> result = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (String part : value.split(";")) {
            String[] assignmentParts = part.trim().split(":", 2);
            if (assignmentParts.length != 2 || assignmentParts[0].isBlank() || assignmentParts[1].isBlank()) {
                throw new IllegalArgumentException("任职格式应为 组织编码:职位");
            }
            String number = assignmentParts[0].trim();
            if (!unique.add(number)) throw new IllegalArgumentException("任职组织编码重复：" + number);
            OrgReference org = orgByNumber.get(number);
            if (org == null) throw new IllegalArgumentException("任职组织编码不存在：" + number);
            UserAssignmentForm assignment = new UserAssignmentForm();
            assignment.setOrgId(org.id());
            assignment.setPosition(assignmentParts[1].trim());
            assignment.setIsPrimary(result.isEmpty());
            assignment.setIsOrgLeader(false);
            result.add(assignment);
        }
        return result;
    }

    private byte[] readAndValidateFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase(java.util.Locale.ROOT).endsWith(".xlsx"))
            throw new BizException(ResultEnum.PARAM_ERROR, "只支持 .xlsx 文件");
        if (file.getSize() <= 0 || file.getSize() > 20L * 1024 * 1024)
            throw new BizException(ResultEnum.PARAM_ERROR, "导入文件必须在 20MB 以内");
        try { return file.getBytes(); } catch (IOException exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "读取导入文件失败");
        }
    }

    private PreparedFileArtifact prepareCredentialFile(List<PlannedUserImportRow> plannedRows, String fileName) {
        List<Credential> created = plannedRows.stream().map(PlannedUserImportRow::credential)
                .filter(java.util.Objects::nonNull).toList();
        if (created.isEmpty()) return null;
        List<? extends List<?>> rows = created.stream()
                .map(item -> List.of(item.username(), item.name(), item.password())).toList();
        byte[] content = excelWorkbookService.write("初始密码", List.of("登录账号", "姓名", "初始密码"), rows);
        return fileArtifactService.prepare(FileStoragePurpose.ONE_TIME_CREDENTIAL, fileName,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content, Duration.ofHours(1), 1);
    }

    private BatchExecution commitBatch(List<PlannedUserImportRow> plannedRows, String fileName) {
        PreparedFileArtifact prepared = prepareCredentialFile(plannedRows, fileName);
        UserImportTxService.BatchCommitResult committed;
        try {
            committed = importTxService.commitBatch(plannedRows.stream().map(PlannedUserImportRow::form).toList(), prepared);
        } catch (RuntimeException exception) {
            if (prepared != null) fileArtifactService.discardQuietly(prepared, exception);
            throw exception;
        }
        List<String> warnings = refreshAfterCommit(plannedRows, committed);
        return new BatchExecution(committed.credentialFile() == null
                ? List.of() : List.of(committed.credentialFile()), warnings);
    }

    /** 数据库提交结果已经确定；授权刷新失败只形成警告，不能把已提交批次改报为写入失败。 */
    private List<String> refreshAfterCommit(List<PlannedUserImportRow> plannedRows,
                                            UserImportTxService.BatchCommitResult committed) {
        List<PlannedUserImportRow> failedRows = new ArrayList<>();
        for (int index = 0; index < plannedRows.size(); index++) {
            PlannedUserImportRow plannedRow = plannedRows.get(index);
            UserSaveForm form = plannedRow.form();
            Long userId = committed.savedIds().get(index);
            try {
                for (Long orgId : committed.previousOrgIds().getOrDefault(userId, List.of())) {
                    authorizationStateHelper.refreshUserAuthorization(userId, orgId);
                }
                authorizationStateHelper.refreshUsers(List.of(userId));
                if (form.getAssignments().isEmpty()) {
                    authorizationStateHelper.terminateUsers(List.of(userId), SessionTerminationReason.ACCOUNT_DISABLED);
                }
            } catch (RuntimeException exception) {
                failedRows.add(plannedRow);
                log.warn("用户导入记录已提交，但授权状态刷新失败: row={}, userId={}",
                        plannedRow.sourceRowNumber(), userId, exception);
            }
        }
        if (failedRows.isEmpty()) return List.of();
        return List.of(formatRowNumbers(failedRows)
                + "已写入成功，但授权状态刷新失败；缓存将在过期后按数据库状态重建");
    }

    private String formatRowNumbers(List<PlannedUserImportRow> plannedRows) {
        List<String> ranges = new ArrayList<>();
        int rangeStart = plannedRows.getFirst().sourceRowNumber();
        int previous = rangeStart;
        for (int index = 1; index < plannedRows.size(); index++) {
            int current = plannedRows.get(index).sourceRowNumber();
            if (current != previous + 1) {
                ranges.add(formatRowRange(rangeStart, previous));
                rangeStart = current;
            }
            previous = current;
        }
        ranges.add(formatRowRange(rangeStart, previous));
        return "第" + String.join("、", ranges) + "行";
    }

    private String credentialFileName(List<PlannedUserImportRow> plannedRows) {
        int first = plannedRows.getFirst().sourceRowNumber();
        int last = plannedRows.getLast().sourceRowNumber();
        String range = first == last ? "第" + first + "行" : "第" + first + "至" + last + "行";
        return "用户初始密码-" + range + ".xlsx";
    }

    private String formatRowRange(int first, int last) {
        return first == last ? String.valueOf(first) : first + "至" + last;
    }

    private FileArtifactReference createErrorFile(List<String> errors) {
        if (errors.isEmpty()) return null;
        List<List<String>> rows = errors.stream().map(List::of).toList();
        byte[] content = excelWorkbookService.write("导入错误", List.of("错误"), rows);
        return fileArtifactService.create(FileStoragePurpose.DATA_IMPORT_ERROR, "用户导入错误报告.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content, Duration.ofHours(24), null);
    }

    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String normalizeEmail(String value) {
        String normalized = emptyToNull(value);
        return normalized == null ? null : normalized.toLowerCase(java.util.Locale.ROOT);
    }
    private record Credential(String username, String name, String password) { }
    private record PlannedUserImportRow(int sourceRowNumber, UserSaveForm form, Credential credential) { }
    private record ImportPlan(List<PlannedUserImportRow> rows, List<String> errors) { }
    private record BatchExecution(List<FileArtifactReference> credentialFiles, List<String> warnings) { }
}
