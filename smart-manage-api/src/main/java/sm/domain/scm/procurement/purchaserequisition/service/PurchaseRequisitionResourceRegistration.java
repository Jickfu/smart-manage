package sm.domain.scm.procurement.purchaserequisition.service;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.scm.procurement.purchaserequisition.constant.PurchaseRequisitionPermission;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionMapper;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.resource.BusinessResourceAccessPolicy;
import sm.system.resource.BusinessResourceAction;
import sm.system.resource.BusinessResourceRegistration;
import sm.system.response.ResultEnum;
import java.util.Set;

/** 采购申请附件资源注册，附件权限继承采购申请的单据权限和暂存状态。 */
@Component
@RequiredArgsConstructor
final class PurchaseRequisitionResourceRegistration implements BusinessResourceRegistration, BusinessResourceAccessPolicy {
    static final String RESOURCE_TYPE = "scm.procurement.purchase-requisition";
    static final String ACTION_VIEW = "VIEW";
    static final String ACTION_SAVE = "SAVE";
    static final String ACTION_DELETE = "DELETE";
    static final String ACTION_SUBMIT = "SUBMIT";
    private final PurchaseRequisitionMapper mapper;
    private final PurchaseRequisitionDataScope dataScope;

    @Override
    public String resourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    public BusinessResourceAccessPolicy accessPolicy() {
        return this;
    }

    @Override
    public boolean supportsDataScope() {
        return true;
    }

    @Override
    public Set<String> dataScopeActions() {
        return Set.of(ACTION_VIEW, ACTION_SAVE, ACTION_DELETE, ACTION_SUBMIT);
    }

    @Override
    public void requireUploadAllowed() {
        StpUtil.checkPermission(PurchaseRequisitionPermission.SAVE);
    }

    @Override
    public void requireAllowed(String resourceId, BusinessResourceAction action) {
        Long purchaseRequisitionId;
        try {
            purchaseRequisitionId = Long.valueOf(resourceId);
        } catch (NumberFormatException exception) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "采购申请资源标识非法");
        }
        PurchaseRequisitionEntity entity = mapper.selectById(purchaseRequisitionId);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "采购申请不存在");
        }
        if (action == BusinessResourceAction.READ) {
            StpUtil.checkPermission(PurchaseRequisitionPermission.DETAIL);
            dataScope.requireAllowed(entity, ACTION_VIEW);
            return;
        }
        StpUtil.checkPermission(PurchaseRequisitionPermission.SAVE);
        dataScope.requireAllowed(entity, ACTION_SAVE);
        if (!BillStatusEnum.SAVED.getValue().equals(entity.getBillStatus())) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "非暂存采购申请不允许维护附件");
        }
    }
}
