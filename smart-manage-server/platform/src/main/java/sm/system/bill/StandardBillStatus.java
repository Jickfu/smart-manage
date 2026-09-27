package sm.system.bill;

import lombok.Getter;

/** 平台保留的标准单据状态；业务扩展不得覆盖 A-D。 */
@Getter
public enum StandardBillStatus {
    SAVED("A", "暂存"),
    SUBMITTED("B", "已提交"),
    AUDITED("C", "已审核"),
    CLOSED("D", "已关闭");

    private final String value;
    private final String name;

    StandardBillStatus(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public BillStatusDefinition definition() {
        return new BillStatusDefinition(value, name);
    }
}
