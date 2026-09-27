package sm.system.bill;

/** 平台提供默认规则的标准单据动作。关闭来源因业务而异，不预设统一转换。 */
public enum StandardBillStatusAction {
    SUBMIT,
    AUDIT,
    CLOSE
}
