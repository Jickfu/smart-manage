package sm.system.excel;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcelWorkbookServiceTests {
    private final ExcelWorkbookService service = new ExcelWorkbookService();

    @Test
    void shouldRoundTripDynamicWorkbook() {
        byte[] content = service.write("用户", List.of("登录账号*", "姓名*"),
                List.of(List.of("zhangsan", "张三")));

        assertThat(service.read(content)).containsExactly(
                java.util.Map.of("登录账号*", "zhangsan", "姓名*", "张三"));
    }

    @Test
    void shouldEscapeFormulaLikeExportText() {
        assertThat(service.safeText("=1+1")).isEqualTo("'=1+1");
        assertThat(service.safeText("+cmd")).isEqualTo("'+cmd");
        assertThat(service.safeText("normal")).isEqualTo("normal");
    }

    @Test
    void shouldRejectFormulaCellsOnImport() throws Exception {
        byte[] content;
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             var output = new java.io.ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("用户");
            sheet.createRow(0).createCell(0).setCellValue("登录账号*");
            sheet.createRow(1).createCell(0).setCellFormula("1+1");
            workbook.write(output);
            content = output.toByteArray();
        }

        assertThatThrownBy(() -> service.read(content)).isInstanceOf(sm.system.exception.BizException.class)
                .hasMessageContaining("不允许包含公式");
    }

    @Test
    void shouldRejectOutdatedHeaders() {
        byte[] content = service.write("用户", List.of("旧表头"), List.of(List.of("值")));

        assertThatThrownBy(() -> service.read(content, List.of("新表头")))
                .isInstanceOf(sm.system.exception.BizException.class)
                .hasMessageContaining("表头");
    }

    @Test
    void shouldRejectTooManyExportRows() {
        List<List<String>> rows = java.util.stream.IntStream.rangeClosed(1, ExcelWorkbookService.MAX_ROWS + 1)
                .mapToObj(index -> List.of("row-" + index)).toList();

        assertThatThrownBy(() -> service.write("用户", List.of("账号"), rows))
                .isInstanceOf(sm.system.exception.BizException.class)
                .hasMessageContaining("最大 10000 行");
    }

    @Test
    void shouldRejectTooLongExportCell() {
        String value = "x".repeat(ExcelWorkbookService.MAX_CELL_LENGTH + 1);

        assertThatThrownBy(() -> service.write("用户", List.of("账号"), List.of(List.of(value))))
                .isInstanceOf(sm.system.exception.BizException.class)
                .hasMessageContaining("最大 4000 字符");
    }
}
