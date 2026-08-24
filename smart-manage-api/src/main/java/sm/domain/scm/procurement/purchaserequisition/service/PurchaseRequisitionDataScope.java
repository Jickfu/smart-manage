package sm.domain.scm.procurement.purchaserequisition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.system.datascope.DataScope;
import sm.system.datascope.DataScopeResolver;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 采购申请对 SELF 的领域解释固定为申请人，而不是创建人。 */
@Component
@RequiredArgsConstructor
final class PurchaseRequisitionDataScope {
    private final DataScopeResolver dataScopeResolver;

    void apply(LambdaQueryWrapper<PurchaseRequisitionEntity> query, String action) {
        apply(query, resolve(action));
    }

    DataScope resolve(String action) {
        return dataScopeResolver.resolve(PurchaseRequisitionResourceRegistration.RESOURCE_TYPE, action);
    }

    void apply(LambdaQueryWrapper<PurchaseRequisitionEntity> query, DataScope scope) {
        if (scope.all()) return;
        query.and(condition -> {
            boolean hasCondition = false;
            if (!scope.orgIds().isEmpty()) {
                condition.in(PurchaseRequisitionEntity::getOrgId, scope.orgIds());
                hasCondition = true;
            }
            if (scope.selfIncluded()) {
                if (hasCondition) condition.or();
                condition.eq(PurchaseRequisitionEntity::getApplicantId, scope.currentUserId());
                hasCondition = true;
            }
            if (!hasCondition) condition.eq(PurchaseRequisitionEntity::getId, -1L);
        });
    }

    void requireAllowed(PurchaseRequisitionEntity entity, String action) {
        DataScope scope = dataScopeResolver.resolve(PurchaseRequisitionResourceRegistration.RESOURCE_TYPE, action);
        if (scope.all() || scope.orgIds().contains(entity.getOrgId())
                || scope.selfIncluded() && scope.currentUserId().equals(entity.getApplicantId())) return;
        throw new BizException(ResultEnum.PERMISSION_ERROR, "无权访问该采购申请");
    }
}
