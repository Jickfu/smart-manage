package sm.domain.scm.procurement.purchaserequisition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionEntryMapper;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionMapper;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntryEntity;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionEntryForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSaveForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSubmitForm;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.util.BillStatusUtil;
import sm.domain.sys.base.attachment.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.service.AttachmentService;

import java.util.Objects;

/** 采购申请包内事务实现，只允许公开 Service 委托调用。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class PurchaseRequisitionTxService {
	private final CurrentUserContext currentUserContext;
    private final PurchaseRequisitionMapper mapper;
    private final PurchaseRequisitionEntryMapper entryMapper;
    private final AttachmentService attachmentService;

    public Long save(PurchaseRequisitionSaveForm form) {
        PurchaseRequisitionEntity entity;
        if (form.getId() == null) {
            entity = new PurchaseRequisitionEntity();
            entity.setOrgId(currentUserContext.getOrgId());
            entity.setApplicantId(currentUserContext.getUserId());
            entity.setBillStatus(BillStatusEnum.SAVED.getValue());
        } else {
            entity = requireEntity(form.getId());
            BillStatusUtil.requireCanSave(entity.getBillStatus());
            requireVersion(entity, form.getVersion());
        }
        entity.setNumber(form.getNumber().trim());
        entity.setSubject(form.getSubject().trim());
        entity.setBizDate(form.getBizDate());
        entity.setRequiredDate(form.getRequiredDate());
        entity.setReason(form.getReason());

        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "新增采购申请失败");
            }
        } else if (mapper.updateById(entity) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "采购申请已被其他用户修改，请刷新后重试");
        }

        // 明细属于采购申请聚合，保存时按请求中的 entrys 整体替换。
        entryMapper.delete(new LambdaQueryWrapper<PurchaseRequisitionEntryEntity>()
                .eq(PurchaseRequisitionEntryEntity::getParentId, entity.getId()));
        int sort = 1;
        for (PurchaseRequisitionEntryForm entryForm : form.getEntrys()) {
            PurchaseRequisitionEntryEntity entry = new PurchaseRequisitionEntryEntity();
            entry.setParentId(entity.getId());
            entry.setMaterialName(entryForm.getMaterialName().trim());
            entry.setSpecification(entryForm.getSpecification());
            entry.setUnit(entryForm.getUnit().trim());
            entry.setQuantity(entryForm.getQuantity());
            entry.setRequiredDate(entryForm.getRequiredDate());
            entry.setRemark(entryForm.getRemark());
            entry.setSort(entryForm.getSort() == null ? sort : entryForm.getSort());
            if (entryMapper.insert(entry) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "采购申请明细写入失败");
            }
            sort++;
        }
        promoteAttachments(form, entity.getId());
        return entity.getId();
    }

    public Long submit(PurchaseRequisitionSubmitForm form) {
        // 保存聚合与推进状态必须处于同一事务，支持新增页和未保存修改直接提交。
        Long id = save(form);
        PurchaseRequisitionEntity entity = requireEntity(id);
        Integer version = entity.getVersion();
        String nextStatus = BillStatusUtil.submit(entity.getBillStatus());
        entity.setBillStatus(nextStatus);
        // version 递增交给 MyBatis-Plus 乐观锁插件，Wrapper 额外保证状态与版本原子匹配。
        int updated = mapper.update(entity, new LambdaUpdateWrapper<PurchaseRequisitionEntity>()
                .eq(PurchaseRequisitionEntity::getId, id)
                .eq(PurchaseRequisitionEntity::getBillStatus, BillStatusEnum.SAVED.getValue())
                .eq(PurchaseRequisitionEntity::getVersion, version));
        if (updated != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "采购申请状态或版本已变化，请刷新后重试");
        }
        return id;
    }

    public void deleteById(Long id, Integer version) {
        PurchaseRequisitionEntity entity = requireEntity(id);
        BillStatusUtil.requireCanSave(entity.getBillStatus());
        requireVersion(entity, version);
        try {
            attachmentService.deleteForAggregate(PurchaseRequisitionResourceRegistration.RESOURCE_TYPE,
                    String.valueOf(id));
        } catch (java.io.IOException exception) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "采购申请附件删除失败: " + exception.getMessage());
        }
        entryMapper.delete(new LambdaQueryWrapper<PurchaseRequisitionEntryEntity>()
                .eq(PurchaseRequisitionEntryEntity::getParentId, id));
        int deleted = mapper.delete(new LambdaQueryWrapper<PurchaseRequisitionEntity>()
                .eq(PurchaseRequisitionEntity::getId, id)
                .eq(PurchaseRequisitionEntity::getVersion, version)
                .eq(PurchaseRequisitionEntity::getBillStatus, BillStatusEnum.SAVED.getValue()));
        if (deleted != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "采购申请状态或版本已变化，请刷新后重试");
        }
    }

    private PurchaseRequisitionEntity requireEntity(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "采购申请ID不能为空");
        }
        PurchaseRequisitionEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "采购申请不存在");
        }
        return entity;
    }

    private void requireVersion(PurchaseRequisitionEntity entity, Integer version) {
        if (version == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "修改采购申请时乐观锁版本号不能为空");
        }
        if (!Objects.equals(entity.getVersion(), version)) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "采购申请已被其他用户修改，请刷新后重试");
        }
    }

    private void promoteAttachments(PurchaseRequisitionSaveForm form, Long purchaseRequisitionId) {
        if (form.getAttachmentIds() == null || form.getAttachmentIds().isEmpty()) {
            return;
        }
        AttachmentPromoteForm attachmentForm = new AttachmentPromoteForm();
        attachmentForm.setAttachmentIds(form.getAttachmentIds().stream().distinct().toList());
        attachmentForm.setUploadSessions(form.getAttachmentUploadSessions());
        attachmentForm.setBizType(PurchaseRequisitionResourceRegistration.RESOURCE_TYPE);
        attachmentForm.setBizId(String.valueOf(purchaseRequisitionId));
        try {
            // 与采购申请写操作加入同一数据库事务；对象键不因 TEMP 提升而移动。
            attachmentService.promoteForAggregate(attachmentForm);
        } catch (java.io.IOException exception) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "采购申请附件确认失败: " + exception.getMessage());
        }
    }
}
