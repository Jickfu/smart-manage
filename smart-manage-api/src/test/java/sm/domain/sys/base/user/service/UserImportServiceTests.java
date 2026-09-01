package sm.domain.sys.base.user.service;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.domain.sys.base.fileartifact.contract.PreparedFileArtifact;
import sm.domain.sys.base.fileartifact.service.FileArtifactService;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.constant.UserExcelSchema;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.enums.UserImportMode;
import sm.domain.sys.base.user.model.enums.UserImportTransactionMode;
import sm.domain.sys.base.user.model.vo.UserImportResultVO;
import sm.system.excel.ExcelWorkbookService;
import sm.system.excel.ExcelDataRow;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class UserImportServiceTests {
    @Test
    void batchFailureReportsOriginalNonContiguousExcelRows() {
        Fixture fixture = fixture(excelRows(
                row("user-1", "用户1", "N001"),
                row("", "非法行", "N002"),
                row("user-3", "用户3", "N003"),
                row("user-4", "用户4", "N004")));
        when(fixture.userMapper().selectList(any())).thenReturn(List.of(
                existing(1L, "user-1", "N001"), existing(3L, "user-3", "N003"),
                existing(4L, "user-4", "N004")), List.of());
        when(fixture.importTxService().commitBatch(any(), any())).thenThrow(new IllegalStateException("database offline"));

        UserImportResultVO result = fixture.service().importUsers(file(), UserImportMode.UPDATE_ONLY,
                UserImportTransactionMode.BATCH);

        assertThat(result.errors()).anyMatch(error -> error.contains("第2、4至5行写入失败"));
        assertThat(result.success()).isZero();
    }

    @Test
    void atomicPrevalidationRejectsDuplicateBusinessNumbersBeforeWrite() {
        Fixture fixture = fixture(excelRows(
                row("user-1", "用户1", "N001"),
                row("user-2", "用户2", "N001")));
        when(fixture.userMapper().selectList(any())).thenReturn(List.of(), List.of());

        UserImportResultVO result = fixture.service().importUsers(file(), UserImportMode.CREATE_ONLY,
                UserImportTransactionMode.ATOMIC);

        assertThat(result.errors()).anyMatch(error -> error.contains("工号与第 2 行重复"));
        verify(fixture.importTxService(), never()).commitBatch(any(), any());
    }

    @Test
    void batchReturnsOneCredentialArtifactForSuccessfulTransactionUnit() {
        Fixture fixture = fixture(excelRows(row("user-1", "用户1", "N001")));
        when(fixture.userMapper().selectList(any())).thenReturn(List.of(), List.of());
        PreparedFileArtifact prepared = mock(PreparedFileArtifact.class);
        when(fixture.fileArtifactService().prepare(any(), any(), any(), any(), any(), any())).thenReturn(prepared);
        FileArtifactReference reference = new FileArtifactReference(10L, "用户初始密码-第2行.xlsx",
                LocalDateTime.now().plusHours(1));
        when(fixture.importTxService().commitBatch(any(), any())).thenReturn(
                new UserImportTxService.BatchCommitResult(List.of(1L), Map.of(), reference));

        UserImportResultVO result = fixture.service().importUsers(file(), UserImportMode.CREATE_ONLY,
                UserImportTransactionMode.BATCH);

        assertThat(result.credentialFiles()).containsExactly(reference);
        assertThat(result.success()).isEqualTo(1);
    }

    @Test
    void authorizationRefreshFailureDoesNotChangeCommittedBatchToFailure() {
        Fixture fixture = fixture(excelRows(row("user-1", "用户1", "N001")));
        when(fixture.userMapper().selectList(any())).thenReturn(List.of(), List.of());
        when(fixture.importTxService().commitBatch(any(), any())).thenReturn(
                new UserImportTxService.BatchCommitResult(List.of(1L), Map.of(), null));
        doThrow(new IllegalStateException("redis offline"))
                .when(fixture.authorizationStateHelper()).refreshUsers(List.of(1L));

        UserImportResultVO result = fixture.service().importUsers(file(), UserImportMode.CREATE_ONLY,
                UserImportTransactionMode.BATCH);

        assertThat(result.success()).isEqualTo(1);
        assertThat(result.failed()).isZero();
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("授权状态刷新失败"));
    }

    private Fixture fixture(List<ExcelDataRow> rows) {
        ExcelWorkbookService excelWorkbookService = mock(ExcelWorkbookService.class);
        UserMapper userMapper = mock(UserMapper.class);
        OrgReferenceReader orgReferenceReader = mock(OrgReferenceReader.class);
        UserImportTxService importTxService = mock(UserImportTxService.class);
        FileArtifactService fileArtifactService = mock(FileArtifactService.class);
        Validator validator = mock(Validator.class);
        AuthorizationStateHelper authorizationStateHelper = mock(AuthorizationStateHelper.class);
        when(excelWorkbookService.read(any(byte[].class), any())).thenReturn(rows);
        when(excelWorkbookService.write(any(), any(), any())).thenReturn(new byte[]{1});
        when(orgReferenceReader.findAll()).thenReturn(List.of());
        when(validator.validate(any())).thenReturn(Set.of());
        UserImportService service = new UserImportService(excelWorkbookService, userMapper, orgReferenceReader,
                importTxService, fileArtifactService, validator, authorizationStateHelper);
        return new Fixture(service, userMapper, importTxService, fileArtifactService, authorizationStateHelper);
    }

    @SafeVarargs
    private final List<ExcelDataRow> excelRows(Map<String, String>... rows) {
        return java.util.stream.IntStream.range(0, rows.length)
                .mapToObj(index -> new ExcelDataRow(index + 2, rows[index]))
                .toList();
    }

    private Map<String, String> row(String username, String name, String number) {
        Map<String, String> row = new LinkedHashMap<>();
        for (String header : UserExcelSchema.IMPORT_HEADERS) row.put(header, "");
        row.put(UserExcelSchema.IMPORT_HEADERS.get(0), username);
        row.put(UserExcelSchema.IMPORT_HEADERS.get(1), name);
        row.put(UserExcelSchema.IMPORT_HEADERS.get(2), number);
        return row;
    }

    private UserEntity existing(Long id, String username, String number) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUsername(username);
        entity.setName(username);
        entity.setNumber(number);
        entity.setVersion(0);
        return entity;
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "users.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1});
    }

    private record Fixture(UserImportService service, UserMapper userMapper, UserImportTxService importTxService,
                           FileArtifactService fileArtifactService,
                           AuthorizationStateHelper authorizationStateHelper) {
    }
}
