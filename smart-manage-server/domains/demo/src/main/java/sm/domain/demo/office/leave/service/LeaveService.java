package sm.domain.demo.office.leave.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import sm.domain.demo.office.leave.converter.LeaveConverter;
import sm.domain.demo.office.leave.mapper.*;
import sm.domain.demo.office.leave.model.entity.*;
import sm.domain.demo.office.leave.model.form.*;
import sm.domain.demo.office.leave.model.vo.LeaveDetailVO;
import sm.domain.sys.base.attachment.contract.AttachmentGateway;
import sm.domain.workflow.process.instance.contract.WorkflowHistoryReader;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.form.PageForm;
import sm.system.response.*;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {
    private final LeaveMapper mapper;
    private final LeaveAttachmentEntryMapper entries;
    private final LeaveAttachmentSnapshotMapper snapshots;
    private final LeaveConverter converter;
    private final LeaveDataScope dataScope;
    private final LeaveTxService transactions;
    private final AttachmentGateway attachments;
    private final WorkflowHistoryReader history;
    private final ObjectMapper json;

    public PageData<LeaveDetailVO> listPage(PageForm form) {
        var query = new LambdaQueryWrapper<LeaveEntity>().orderByDesc(LeaveEntity::getId);
        dataScope.apply(query, "VIEW");
        var page = mapper.selectPage(Page.of(form.getPageNum(), form.getPageSize()), query);
        return PageData.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords().stream().map(converter::toDetailVO).toList());
    }

    public LeaveDetailVO detail(Long id) {
        var entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "请假申请不存在");
        dataScope.requireAllowed(entity, "VIEW");
        var detail = converter.toDetailVO(entity);
        Set<Long> selected = entries.selectList(new LambdaQueryWrapper<LeaveAttachmentEntryEntity>()
                .eq(LeaveAttachmentEntryEntity::getParentId, id)).stream().map(LeaveAttachmentEntryEntity::getAttachmentId).collect(Collectors.toSet());
        detail.setAttachments(attachments.listByBiz(LeaveResourceRegistration.RESOURCE_TYPE, id.toString()).stream()
                .filter(attachment -> selected.contains(attachment.getId())).toList());
        detail.setRetainedAttachmentIds(snapshots.selectList(new LambdaQueryWrapper<LeaveAttachmentSnapshotEntity>()
                .eq(LeaveAttachmentSnapshotEntity::getLeaveId, id)).stream().map(LeaveAttachmentSnapshotEntity::getAttachmentId).distinct().toList());
        return detail;
    }

    public LeaveDetailVO approvalDetail(LeaveApprovalDetailForm form) {
        return json.readValue(history.requireSnapshot(LeaveResourceRegistration.BUSINESS_TYPE, form.id(), form.instanceId()), LeaveDetailVO.class);
    }

    @BizLog(value = "保存请假申请", recordRequest = false)
    public Long save(LeaveSaveForm form) { return transactions.save(form); }
    @BizLog(value = "提交请假申请", recordRequest = false)
    public Long submit(LeaveSubmitForm form) { return transactions.submit(form); }
    @BizLog("删除请假草稿")
    public void delete(LeaveDeleteForm form) { transactions.delete(form); }
}
