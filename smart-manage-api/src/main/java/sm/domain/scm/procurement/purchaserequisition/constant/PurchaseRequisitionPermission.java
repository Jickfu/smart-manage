package sm.domain.scm.procurement.purchaserequisition.constant;

/** 采购申请权限码。 */
public final class PurchaseRequisitionPermission {
    public static final String PREFIX = "scm:procurement:purchase-requisition";
    public static final String LIST = PREFIX + ":listPage";
    public static final String DETAIL = PREFIX + ":detail";
    public static final String SAVE = PREFIX + ":save";
    public static final String SUBMIT = PREFIX + ":submit";
    public static final String DELETE = PREFIX + ":delete";
    public static final String EXPORT = PREFIX + ":export";

    private PurchaseRequisitionPermission() {
    }
}
