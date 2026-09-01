package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.service.FileArtifactService;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
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
@RequiredArgsConstructor
public class UserImportService {
    private static final List<String> HEADERS = UserExcelSchema.IMPORT_HEADERS;
    private final ExcelWorkbookService excelWorkbookService;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final OrgReferenceReader orgReferenceReader;
    private final UserTxService txService;
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
            return new UserImportResultVO(rows.size(), 0, rows.size(), plan.errors(), null,
                    createErrorFile(plan.errors()));
        }

        List<Credential> credentials = new ArrayList<>();
        int success = 0;
        List<String> errors = new ArrayList<>(plan.errors());
        if (transactionMode == UserImportTransactionMode.ATOMIC) {
            saveAndRefresh(plan.forms());
            credentials.addAll(plan.credentials());
            success = plan.forms().size();
        } else {
            final int batchSize = 200;
            for (int start = 0; start < plan.forms().size(); start += batchSize) {
                int end = Math.min(start + batchSize, plan.forms().size());
                try {
                    List<UserSaveForm> batchForms = plan.forms().subList(start, end);
                    saveAndRefresh(batchForms);
                    credentials.addAll(plan.credentials().subList(start, end));
                    success += end - start;
                } catch (RuntimeException exception) {
                    errors.add("第 " + (start + 2) + " 至 " + (end + 1) + " 行写入失败：" + exception.getMessage());
                }
            }
        }
        FileArtifactReference credentialFile = createCredentialFile(credentials);
        return new UserImportResultVO(rows.size(), success, rows.size() - success, errors, credentialFile,
                createErrorFile(errors));
    }

    private ImportPlan buildPlan(List<Map<String, String>> rows, UserImportMode mode) {
        Map<String, UserEntity> existingByUsername = new HashMap<>();
        for (UserEntity entity : userMapper.selectList(new LambdaQueryWrapper<UserEntity>())) {
            existingByUsername.put(entity.getUsername(), entity);
        }
        Map<String, OrgReference> orgByNumber = new HashMap<>();
        for (OrgReference org : orgReferenceReader.findAll()) orgByNumber.put(org.number(), org);
        List<UserSaveForm> forms = new ArrayList<>();
        List<Credential> credentials = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> usernames = new HashSet<>();
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
            forms.add(form);
            credentials.add(credential);
        }
        return new ImportPlan(forms, credentials, errors);
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

    private FileArtifactReference createCredentialFile(List<Credential> credentials) {
        List<Credential> created = credentials.stream().filter(java.util.Objects::nonNull).toList();
        if (created.isEmpty()) return null;
        List<? extends List<?>> rows = created.stream()
                .map(item -> List.of(item.username(), item.name(), item.password())).toList();
        byte[] content = excelWorkbookService.write("初始密码", List.of("登录账号", "姓名", "初始密码"), rows);
        return fileArtifactService.create(FileStoragePurpose.ONE_TIME_CREDENTIAL, "用户导入初始密码.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content, Duration.ofHours(1), 1);
    }

    /** 批次提交后刷新授权快照；被移除全部任职的账号立即终止会话。 */
    private void saveAndRefresh(List<UserSaveForm> forms) {
        Map<Long, List<Long>> previousOrgIds = new HashMap<>();
        for (UserSaveForm form : forms) {
            if (form.getId() != null) previousOrgIds.put(form.getId(), userRoleMapper.selectOrgIdsByUserId(form.getId()));
        }
        List<Long> savedIds = txService.saveBatch(forms);
        for (int index = 0; index < forms.size(); index++) {
            UserSaveForm form = forms.get(index);
            Long userId = savedIds.get(index);
            for (Long orgId : previousOrgIds.getOrDefault(userId, List.of())) {
                authorizationStateHelper.refreshUserAuthorization(userId, orgId);
            }
            authorizationStateHelper.refreshUsers(List.of(userId));
            if (form.getAssignments().isEmpty()) {
                authorizationStateHelper.terminateUsers(List.of(userId), SessionTerminationReason.ACCOUNT_DISABLED);
            }
        }
    }

    private FileArtifactReference createErrorFile(List<String> errors) {
        if (errors.isEmpty()) return null;
        List<List<String>> rows = errors.stream().map(List::of).toList();
        byte[] content = excelWorkbookService.write("导入错误", List.of("错误"), rows);
        return fileArtifactService.create(FileStoragePurpose.DATA_IMPORT_ERROR, "用户导入错误报告.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content, Duration.ofHours(24), null);
    }

    private String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private record Credential(String username, String name, String password) { }
    private record ImportPlan(List<UserSaveForm> forms, List<Credential> credentials, List<String> errors) { }
}
