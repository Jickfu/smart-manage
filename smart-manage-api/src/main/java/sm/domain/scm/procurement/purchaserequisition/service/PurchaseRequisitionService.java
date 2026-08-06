package sm.domain.scm.procurement.purchaserequisition.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionEntryMapper;
import sm.domain.scm.procurement.purchaserequisition.mapper.PurchaseRequisitionMapper;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntity;
import sm.domain.scm.procurement.purchaserequisition.model.entity.PurchaseRequisitionEntryEntity;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionListForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSaveForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSubmitForm;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionCreateNewDataVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionDetailVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionEntryVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionListVO;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.system.aop.log.BizLog;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.domain.sys.base.attachment.service.AttachmentService;

import java.time.LocalDate;
import java.util.List;

/** 采购申请聚合的唯一公开服务。 */
@Service
@RequiredArgsConstructor
public class PurchaseRequisitionService {
	private final CurrentUserContext currentUserContext;
    private final PurchaseRequisitionMapper mapper;
    private final PurchaseRequisitionEntryMapper entryMapper;
    private final PurchaseRequisitionTxService txService;
    private final PurchaseRequisitionConverter converter;
    private final AttachmentService attachmentService;

    public PageData<PurchaseRequisitionListVO> listPage(PurchaseRequisitionListForm form) {
        LambdaQueryWrapper<PurchaseRequisitionEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            queryWrapper.and(condition -> condition.like(PurchaseRequisitionEntity::getNumber, keyword)
                    .or().like(PurchaseRequisitionEntity::getSubject, keyword));
        }
        queryWrapper.eq(form.getBillStatus() != null && !form.getBillStatus().isBlank(),
                PurchaseRequisitionEntity::getBillStatus, form.getBillStatus());
        queryWrapper.orderByDesc(PurchaseRequisitionEntity::getCreateTime);
        Page<PurchaseRequisitionEntity> page = mapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), queryWrapper);
        List<PurchaseRequisitionListVO> records = page.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    public PurchaseRequisitionDetailVO detail(Long id) {
        PurchaseRequisitionEntity entity = requireEntity(id);
        PurchaseRequisitionDetailVO detailVO = converter.toDetailVO(entity);
        // 明细查询属于聚合组装，不放入仅承担纯字段映射的 Converter。
        detailVO.setEntrys(entryMapper.selectList(new LambdaQueryWrapper<PurchaseRequisitionEntryEntity>()
                        .eq(PurchaseRequisitionEntryEntity::getParentId, id)
                        .orderByAsc(PurchaseRequisitionEntryEntity::getSort)
                        .orderByAsc(PurchaseRequisitionEntryEntity::getId))
                .stream().map(converter::toEntryVO).toList());
        detailVO.setAttachments(attachmentService.listByBiz(PurchaseRequisitionResourceRegistration.RESOURCE_TYPE,
                String.valueOf(id)));
        return detailVO;
    }

    public PurchaseRequisitionCreateNewDataVO createNewData() {
        PurchaseRequisitionCreateNewDataVO createNewDataVO = new PurchaseRequisitionCreateNewDataVO();
        createNewDataVO.setApplyOrgId(currentUserContext.getOrgId());
        createNewDataVO.setApplicantId(currentUserContext.getUserId());
        createNewDataVO.setBizDate(LocalDate.now());
        createNewDataVO.setBillStatus(BillStatusEnum.SAVED.getValue());
        return createNewDataVO;
    }

    @BizLog("保存采购申请")
    public Long save(PurchaseRequisitionSaveForm form) {
        return txService.save(form);
    }

    @BizLog("提交采购申请")
    public Long submit(PurchaseRequisitionSubmitForm form) {
        return txService.submit(form);
    }

    @BizLog("删除采购申请")
    public void deleteById(Long id, Integer version) {
        txService.deleteById(id, version);
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

}
