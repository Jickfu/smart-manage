package sm.domain.scm.procurement.purchaserequisition.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntryEntity;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionDetailVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionEntryVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionListVO;
import sm.infrastructure.mapping.SmMapperConfig;

/** 采购申请纯字段转换器，不承担聚合查询和业务规则。 */
@Mapper(config = SmMapperConfig.class)
interface PurchaseRequisitionConverter {

    PurchaseRequisitionListVO toListVO(PurchaseRequisitionEntity entity);

    @Mapping(target = "entries", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    PurchaseRequisitionDetailVO toDetailVO(PurchaseRequisitionEntity entity);

    PurchaseRequisitionEntryVO toEntryVO(PurchaseRequisitionEntryEntity entity);
}
