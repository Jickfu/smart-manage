package sm.system.excel;

import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.read.listener.ReadListener;
import org.springframework.stereotype.Component;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 隔离 Apache Fesod 的最小 Excel 技术边界，领域模型不引用第三方注解或类型。 */
@Component
public class ExcelWorkbookService {
    public static final int MAX_ROWS = 10_000;
    public static final int MAX_COLUMNS = 64;
    public static final int MAX_CELL_LENGTH = 4_000;

    public byte[] write(String sheetName, List<String> headers, List<? extends List<?>> rows) {
        List<List<String>> head = headers.stream().map(List::of).toList();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FesodSheet.write(outputStream).head(head).sheet(sheetName).doWrite(rows);
        return outputStream.toByteArray();
    }

    public List<Map<String, String>> read(byte[] content) {
        return readInternal(content, null);
    }

    public List<Map<String, String>> read(byte[] content, List<String> expectedHeaders) {
        return readInternal(content, expectedHeaders);
    }

    private List<Map<String, String>> readInternal(byte[] content, List<String> expectedHeaders) {
        validateSafeWorkbook(content);
        List<Map<Integer, String>> rawRows = new ArrayList<>();
        FesodSheet.read(new ByteArrayInputStream(content), new ReadListener<Map<Integer, String>>() {
            @Override public void invoke(Map<Integer, String> row, AnalysisContext context) {
                if (rawRows.size() >= MAX_ROWS) throw new BizException(ResultEnum.PARAM_ERROR, "Excel 超过最大 10000 行");
                rawRows.add(new LinkedHashMap<>(row));
            }
            @Override public void doAfterAllAnalysed(AnalysisContext context) { }
        }).headRowNumber(0).sheet(0).doRead();
        if (rawRows.isEmpty()) throw new BizException(ResultEnum.PARAM_ERROR, "Excel 数据为空");
        Map<Integer, String> header = rawRows.removeFirst();
        if (header.size() > MAX_COLUMNS) throw new BizException(ResultEnum.PARAM_ERROR, "Excel 超过最大 64 列");
        if (expectedHeaders != null && !new ArrayList<>(header.values()).equals(expectedHeaders)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "Excel 表头与当前模板不一致，请重新下载模板");
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<Integer, String> rawRow : rawRows) {
            Map<String, String> row = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> column : header.entrySet()) {
                String value = rawRow.get(column.getKey());
                if (value != null && value.length() > MAX_CELL_LENGTH) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "Excel 单元格超过最大 4000 字符");
                }
                row.put(column.getValue(), value == null ? "" : value.trim());
            }
            if (row.values().stream().anyMatch(value -> !value.isBlank())) result.add(row);
        }
        return result;
    }

    /** 在映射业务行前拒绝公式和外部链接，避免导入文件携带可执行或远程引用语义。 */
    private void validateSafeWorkbook(byte[] content) {
        try (OPCPackage packageFile = OPCPackage.open(new ByteArrayInputStream(content));
             XSSFWorkbook workbook = new XSSFWorkbook(packageFile)) {
            if (workbook.getNumberOfSheets() != 1) {
                throw new BizException(ResultEnum.PARAM_ERROR, "Excel 必须且只能包含一个工作表");
            }
            if (!workbook.getExternalLinksTable().isEmpty()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "Excel 不允许包含外部链接");
            }
            for (var sheet : workbook) {
                if (sheet.getPhysicalNumberOfRows() > MAX_ROWS + 1) {
                    throw new BizException(ResultEnum.PARAM_ERROR, "Excel 超过最大 10000 行");
                }
                for (var row : sheet) {
                    if (row.getLastCellNum() > MAX_COLUMNS) {
                        throw new BizException(ResultEnum.PARAM_ERROR, "Excel 超过最大 64 列");
                    }
                    for (var cell : row) {
                        if (cell.getCellType() == CellType.FORMULA) {
                            throw new BizException(ResultEnum.PARAM_ERROR, "Excel 不允许包含公式");
                        }
                    }
                }
            }
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ResultEnum.PARAM_ERROR, "Excel 文件结构无效");
        }
    }

    /** 普通文本阻断 Excel 公式解释，不改变业务显示内容。 */
    public String safeText(String value) {
        if (value == null || value.isEmpty()) return value;
        return "=+-@".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
    }
}
