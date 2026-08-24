package sm.domain.scm.procurement.purchaserequisition.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sm.domain.scm.procurement.purchaserequisition.constant.PurchaseRequisitionPermission;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionListForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionDeleteForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSaveForm;
import sm.domain.scm.procurement.purchaserequisition.model.form.PurchaseRequisitionSubmitForm;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionCreateNewDataVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionDetailVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionListVO;
import sm.domain.scm.procurement.purchaserequisition.model.vo.PurchaseRequisitionHomeSummaryVO;
import sm.domain.scm.procurement.purchaserequisition.service.PurchaseRequisitionService;
import sm.system.form.IdForm;
import sm.system.response.PageData;
import sm.system.response.Result;

@RestController
@RequiredArgsConstructor
@Tag(name = "采购管理-采购申请", description = "采购申请单接口")
public class PurchaseRequisitionController {
    private final PurchaseRequisitionService service;

    @PostMapping("/scm/procurement/purchase-requisition/listPage")
    @SaCheckPermission(PurchaseRequisitionPermission.LIST)
    public Result<PageData<PurchaseRequisitionListVO>> listPage(
            @RequestBody PurchaseRequisitionListForm form) {
        return Result.success(service.listPage(form));
    }

    @PostMapping("/scm/procurement/purchase-requisition/detail")
    @SaCheckPermission(PurchaseRequisitionPermission.DETAIL)
    public Result<PurchaseRequisitionDetailVO> detail(@RequestBody @Valid IdForm form) {
        return Result.success(service.detail(form.getId()));
    }

    @GetMapping("/scm/procurement/purchase-requisition/createNewData")
    @SaCheckPermission(PurchaseRequisitionPermission.SAVE)
    public Result<PurchaseRequisitionCreateNewDataVO> createNewData() {
        return Result.success(service.createNewData());
    }

    @GetMapping("/scm/procurement/purchase-requisition/home-summary")
    @SaCheckPermission(PurchaseRequisitionPermission.LIST)
    public Result<PurchaseRequisitionHomeSummaryVO> homeSummary() {
        return Result.success(service.homeSummary());
    }

    @PostMapping("/scm/procurement/purchase-requisition/save")
    @SaCheckPermission(PurchaseRequisitionPermission.SAVE)
    public Result<Long> save(@RequestBody @Valid PurchaseRequisitionSaveForm form) {
        return Result.success(service.save(form));
    }

    @PostMapping("/scm/procurement/purchase-requisition/submit")
    @SaCheckPermission(PurchaseRequisitionPermission.SUBMIT)
    public Result<Long> submit(@RequestBody @Valid PurchaseRequisitionSubmitForm form) {
        return Result.success(service.submit(form));
    }

    @PostMapping("/scm/procurement/purchase-requisition/delete")
    @SaCheckPermission(PurchaseRequisitionPermission.DELETE)
    public Result<String> delete(@RequestBody @Valid PurchaseRequisitionDeleteForm form) {
        service.deleteById(form.getId(), form.getVersion());
        return Result.success();
    }
}
