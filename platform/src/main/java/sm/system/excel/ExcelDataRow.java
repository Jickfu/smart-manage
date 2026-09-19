package sm.system.excel;

import java.util.Map;

/** Excel 数据行及其从 1 开始计数的物理行号。 */
public record ExcelDataRow(int rowNumber, Map<String, String> values) {
}
