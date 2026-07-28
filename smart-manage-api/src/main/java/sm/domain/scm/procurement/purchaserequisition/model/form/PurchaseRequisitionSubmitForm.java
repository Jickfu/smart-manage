package sm.domain.scm.procurement.purchaserequisition.model.form;

/**
 * 采购申请提交参数。
 *
 * <p>提交必须携带页面当前的完整聚合数据，使新增或编辑后的单据可以直接提交，
 * 不要求用户先执行一次保存。已有单据仍通过继承字段中的 id 和 version 执行并发校验。
 */
public class PurchaseRequisitionSubmitForm extends PurchaseRequisitionSaveForm {
}
