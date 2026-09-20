package sm.domain.sys.monitor.alert.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/** 告警指标统一展示格式；数据库和状态机继续使用原始标准值。 */
@Component
class MonitorMetricValueFormatter {
  String format(BigDecimal value, String valueKind, String displayUnit) {
    if (value == null) return "-";
    return switch (valueKind) {
      case "RATIO" -> decimal(value.multiply(BigDecimal.valueOf(100))) + "%";
      case "BOOLEAN" -> value.compareTo(BigDecimal.ZERO) > 0 ? "异常" : "正常";
      case "COUNT" -> decimal(value) + " 个";
      default -> decimal(value) + unit(displayUnit);
    };
  }

  private String decimal(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
  }

  private String unit(String value) {
    return value == null || value.isBlank() ? "" : " " + value;
  }
}
