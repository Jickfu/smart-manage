package sm.system.bill;

/**
 * 单据状态定义。
 *
 * <p>状态编码固定为一个大写 ASCII 字母。A-D 由平台保留，E-Z 可由具体业务聚合扩展。
 *
 * @param value 数据库存储值
 * @param name 中文名称
 */
public record BillStatusDefinition(String value, String name) {

    public BillStatusDefinition {
        if (value == null || !value.matches("[A-Z]")) {
            throw new IllegalArgumentException("单据状态编码必须是一个大写 ASCII 字母");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("单据状态名称不能为空");
        }
        name = name.trim();
    }
}
