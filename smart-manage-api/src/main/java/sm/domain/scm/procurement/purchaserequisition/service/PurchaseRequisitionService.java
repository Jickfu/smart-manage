package sm.domain.scm.procurement.purchaserequisition.service;

import sm.domain.scm.procurement.purchaserequisition.converter.PurchaseRequisitionConverter;

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
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionExportForm;
import sm.domain.sys.base.fileartifact.contract.FileArtifactGateway;
import sm.domain.sys.base.fileartifact.contract.FileArtifactReference;
import sm.system.excel.ExcelWorkbookService;
import sm.system.storage.FileStoragePurpose;
import java.time.Duration;
import java.util.ArrayList;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionCreateNewDataVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionDetailVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionEntryVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionListVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionHomeSummaryVO;
import sm.system.security.context.CurrentUserContext;
import sm.system.aop.log.BizLog;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListQueryUtil;
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.contract.UserReference;
import sm.domain.sys.base.user.contract.UserReferenceReader;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import sm.system.datascope.DataScope;

/** 采购申请聚合的唯一公开服务。 */
@Service
@RequiredArgsConstructor
public class PurchaseRequisitionService {
	private static final Map<String, ListQueryUtil.Field<PurchaseRequisitionEntity>> LIST_FIELDS = Map.of(
			"number", ListQueryUtil.string(PurchaseRequisitionEntity::getNumber, true),
			"subject", ListQueryUtil.string(PurchaseRequisitionEntity::getSubject, true),
			"bizDate", ListQueryUtil.date(PurchaseRequisitionEntity::getBizDate, true),
			"requiredDate", ListQueryUtil.date(PurchaseRequisitionEntity::getRequiredDate, true),
			"billStatus", ListQueryUtil.enumeration(PurchaseRequisitionEntity::getBillStatus, true),
			"createTime", ListQueryUtil.dateTime(PurchaseRequisitionEntity::getCreateTime, true));
	private final CurrentUserContext currentUserContext;
    private final PurchaseRequisitionMapper mapper;
    private final PurchaseRequisitionEntryMapper entryMapper;
    private final PurchaseRequisitionTxService txService;
    private final PurchaseRequisitionConverter converter;
    private final AttachmentGateway attachmentGateway;
    private final PurchaseRequisitionDataScope dataScope;
    private final ExcelWorkbookService excelWorkbookService;
    private final FileArtifactGateway fileArtifactGateway;
    private final OrgReferenceReader orgReferenceReader;
    private final UserReferenceReader userReferenceReader;

    @BizLog("导出采购申请")
    public FileArtifactReference export(PurchaseRequisitionExportForm form) {
        LambdaQueryWrapper<PurchaseRequisitionEntity> query = buildListQuery(form);
        if (form.getIds() != null && !form.getIds().isEmpty()) query.in(PurchaseRequisitionEntity::getId, form.getIds());
        query.last("LIMIT 10001");
        List<PurchaseRequisitionEntity> entities = mapper.selectList(query);
        if (entities.size() > 10_000) throw new BizException(ResultEnum.PARAM_ERROR, "单次最多导出 10000 条采购申请");
        List<Long> parentIds = entities.stream().map(PurchaseRequisitionEntity::getId).toList();
        Map<Long, List<PurchaseRequisitionEntryEntity>> entriesByParent = new LinkedHashMap<>();
        if (!parentIds.isEmpty()) {
            for (PurchaseRequisitionEntryEntity entry : entryMapper.selectList(
                    new LambdaQueryWrapper<PurchaseRequisitionEntryEntity>().in(PurchaseRequisitionEntryEntity::getParentId, parentIds)
                            .orderByAsc(PurchaseRequisitionEntryEntity::getParentId).orderByAsc(PurchaseRequisitionEntryEntity::getSort))) {
                entriesByParent.computeIfAbsent(entry.getParentId(), ignored -> new ArrayList<>()).add(entry);
            }
        }
        List<String> headers = switch (form.getLayout()) {
            case EXPORT_TEMPLATE -> List.of("编码", "主题", "所属组织编码", "所属组织名称", "申请人工号", "申请人姓名",
                    "业务日期", "需求日期", "单据状态", "申请原因", "物料", "规格", "单位", "数量", "明细需求日期", "明细备注");
            case IMPORT_TEMPLATE -> List.of("编码", "主题*", "所属组织编码*", "申请人工号*", "业务日期*", "需求日期",
                    "申请原因", "物料*", "规格", "单位*", "数量*", "明细需求日期", "明细备注");
        };
        List<List<?>> rows = new ArrayList<>();
        for (PurchaseRequisitionEntity entity : entities) {
            OrgReference org = orgReferenceReader.require(entity.getOrgId());
            UserReference applicant = userReferenceReader.require(entity.getApplicantId());
            List<PurchaseRequisitionEntryEntity> entries = entriesByParent.getOrDefault(entity.getId(), List.of());
            if (entries.isEmpty()) rows.add(exportRow(entity, null, org, applicant, form.getLayout()));
            else for (PurchaseRequisitionEntryEntity entry : entries) rows.add(exportRow(entity, entry, org, applicant, form.getLayout()));
        }
        byte[] content = excelWorkbookService.write("采购申请", headers, rows);
        return fileArtifactGateway.create(FileStoragePurpose.DATA_EXPORT_RESULT, exportFileName(form.getLayout()),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", content, Duration.ofHours(24), null);
    }

    private String exportFileName(sm.system.excel.DataExportLayout layout) {
        return switch (layout) {
            case EXPORT_TEMPLATE -> "采购申请-完整信息.xlsx";
            case IMPORT_TEMPLATE -> "采购申请-导入模板.xlsx";
        };
    }

    private List<?> exportRow(PurchaseRequisitionEntity entity, PurchaseRequisitionEntryEntity entry,
                              OrgReference org, UserReference applicant, sm.system.excel.DataExportLayout layout) {
        List<Object> common = new ArrayList<>(java.util.Arrays.asList(safe(entity.getNumber()), safe(entity.getSubject()),
                safe(org.number())));
        if (layout == sm.system.excel.DataExportLayout.EXPORT_TEMPLATE) {
            common.add(safe(org.name()));
            common.add(safe(applicant.number()));
            common.add(safe(applicant.name()));
            common.add(entity.getBizDate());
            common.add(entity.getRequiredDate() == null ? "" : entity.getRequiredDate());
            common.add(entity.getBillStatus());
        } else {
            common.add(safe(applicant.number()));
            common.add(entity.getBizDate());
            common.add(entity.getRequiredDate() == null ? "" : entity.getRequiredDate());
        }
        common.addAll(java.util.Arrays.asList(safe(entity.getReason()), entry == null ? "" : safe(entry.getMaterialName()),
                entry == null ? "" : safe(entry.getSpecification()), entry == null ? "" : safe(entry.getUnit()),
                entry == null ? "" : entry.getQuantity(), entry == null ? "" : entry.getRequiredDate(),
                entry == null ? "" : safe(entry.getRemark())));
        return common;
    }

    private String safe(String value) { return excelWorkbookService.safeText(value == null ? "" : value); }

    public PageData<PurchaseRequisitionListVO> listPage(PurchaseRequisitionListForm form) {
        LambdaQueryWrapper<PurchaseRequisitionEntity> queryWrapper = buildListQuery(form);
        Page<PurchaseRequisitionEntity> page = mapper.selectPage(
                new Page<>(form.getPageNum(), form.getPageSize()), queryWrapper);
        List<PurchaseRequisitionListVO> records = page.getRecords().stream().map(converter::toListVO).toList();
        return PageData.of(page.getTotal(), form.getPageNum(), form.getPageSize(), records);
    }

    private LambdaQueryWrapper<PurchaseRequisitionEntity> buildListQuery(PurchaseRequisitionListForm form) {
        LambdaQueryWrapper<PurchaseRequisitionEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String keyword = form.getKeyword().trim();
            queryWrapper.and(condition -> condition.like(PurchaseRequisitionEntity::getNumber, keyword)
                    .or().like(PurchaseRequisitionEntity::getSubject, keyword));
        }
        queryWrapper.eq(form.getBillStatus() != null && !form.getBillStatus().isBlank(),
                PurchaseRequisitionEntity::getBillStatus, form.getBillStatus());
        dataScope.apply(queryWrapper, PurchaseRequisitionResourceRegistration.ACTION_VIEW);
        ListQueryUtil.apply(queryWrapper, form, LIST_FIELDS);
        if (!ListQueryUtil.hasSort(form)) queryWrapper.orderByDesc(PurchaseRequisitionEntity::getCreateTime);
        if (!ListQueryUtil.isSortedBy(form, "id")) queryWrapper.orderByDesc(PurchaseRequisitionEntity::getId);
        return queryWrapper;
    }

    public PurchaseRequisitionDetailVO detail(Long id) {
        PurchaseRequisitionEntity entity = requireEntity(id);
        dataScope.requireAllowed(entity, PurchaseRequisitionResourceRegistration.ACTION_VIEW);
        PurchaseRequisitionDetailVO detailVO = converter.toDetailVO(entity);
        // 明细查询属于聚合组装，不放入仅承担纯字段映射的 Converter。
        detailVO.setEntries(entryMapper.selectList(new LambdaQueryWrapper<PurchaseRequisitionEntryEntity>()
                        .eq(PurchaseRequisitionEntryEntity::getParentId, id)
                        .orderByAsc(PurchaseRequisitionEntryEntity::getSort)
                        .orderByAsc(PurchaseRequisitionEntryEntity::getId))
                .stream().map(converter::toEntryVO).toList());
        detailVO.setAttachments(attachmentGateway.listByBiz(PurchaseRequisitionResourceRegistration.RESOURCE_TYPE,
                String.valueOf(id)));
        return detailVO;
    }

    public PurchaseRequisitionCreateNewDataVO createNewData() {
        PurchaseRequisitionCreateNewDataVO createNewDataVO = new PurchaseRequisitionCreateNewDataVO();
        createNewDataVO.setOrgId(currentUserContext.getOrgId());
        createNewDataVO.setApplicantId(currentUserContext.getUserId());
        createNewDataVO.setBizDate(LocalDate.now());
        createNewDataVO.setBillStatus(BillStatusEnum.SAVED.getValue());
        return createNewDataVO;
    }

    /** 首页只查询当前用户 VIEW 数据范围内的轻量统计与最近记录。 */
    public PurchaseRequisitionHomeSummaryVO homeSummary() {
        DataScope viewScope = dataScope.resolve(PurchaseRequisitionResourceRegistration.ACTION_VIEW);
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (BillStatusEnum status : BillStatusEnum.values()) {
            String statusValue = status.getValue();
            statusCounts.put(statusValue, 0L);
        }
        for (Map<String, Object> row : mapper.selectStatusCounts(viewScope)) {
            String statusValue = (String) row.get("billStatus");
            BillStatusEnum.fromValue(statusValue);
            statusCounts.put(statusValue, ((Number) row.get("total")).longValue());
        }
        LambdaQueryWrapper<PurchaseRequisitionEntity> recentQuery = new LambdaQueryWrapper<>();
        dataScope.apply(recentQuery, viewScope);
        recentQuery.orderByDesc(PurchaseRequisitionEntity::getCreateTime)
                .orderByDesc(PurchaseRequisitionEntity::getId).last("LIMIT 5");
        PurchaseRequisitionHomeSummaryVO summary = new PurchaseRequisitionHomeSummaryVO();
        summary.setStatusCounts(statusCounts);
        summary.setRecent(mapper.selectList(recentQuery).stream().map(converter::toListVO).toList());
        return summary;
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
        dataScope.requireAllowed(requireEntity(id), PurchaseRequisitionResourceRegistration.ACTION_DELETE);
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
