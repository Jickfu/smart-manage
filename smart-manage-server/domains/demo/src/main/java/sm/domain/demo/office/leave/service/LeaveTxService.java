package sm.domain.demo.office.leave.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import sm.domain.demo.office.leave.mapper.*;
import sm.domain.demo.office.leave.model.entity.*;
import sm.domain.demo.office.leave.model.form.*;
import sm.domain.demo.office.leave.converter.LeaveConverter;
import sm.domain.sys.base.attachment.contract.*;
import sm.domain.sys.base.numberrule.contract.NumberGenerator;
import sm.domain.sys.base.numberrule.contract.model.NumberGenerationContext;
import sm.domain.workflow.process.runtime.contract.WorkflowSubmission;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class LeaveTxService {
    private final LeaveMapper mapper;
    private final LeaveAttachmentEntryMapper entries;
    private final LeaveAttachmentSnapshotMapper snapshots;
    private final LeaveConverter converter;
    private final LeaveDataScope dataScope;
    private final CurrentUserContext currentUser;
    private final NumberGenerator numbers;
    private final AttachmentGateway attachments;
    private final WorkflowSubmission workflow;
    private final ObjectMapper json;

    Long save(LeaveSaveForm form) { return saveAggregate(form, "SAVE").getId(); }

    Long submit(LeaveSubmitForm form) {
        mapper.lockSubmission(form.getRequestId());
        String requestDigest = fingerprint(form);
        var previous = workflow.findSubmission(form.getRequestId(), requestDigest);
        if (previous != null) {
            var original = mapper.selectById(previous.businessId());
            if (!LeaveResourceRegistration.BUSINESS_TYPE.equals(previous.businessType()) || original == null
                    || !Objects.equals(original.getClientKey(), form.getClientKey())
                    || form.getId() != null && !Objects.equals(original.getId(), form.getId())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "提交请求标识与单据不匹配");
            }
            return original.getId();
        }
        var entity = saveAggregate(form, "SUBMIT");
        var snapshot = converter.toDetailVO(entity);
        var selected = Set.copyOf(form.getAttachmentIds());
        snapshot.setAttachments(attachments.listForAggregate(LeaveResourceRegistration.RESOURCE_TYPE, entity.getId().toString())
                .stream().filter(attachment -> selected.contains(attachment.getId())).toList());
        snapshot.setBillStatus("B");
        snapshot.setCurrentInstanceId(null);
        Long instanceId = workflow.submit(new WorkflowSubmission.Command(LeaveResourceRegistration.BUSINESS_TYPE, entity.getId(),
                entity.getNumber(), entity.getOrgId(), entity.getApplicantId(), form.getRequestId(), requestDigest, json.writeValueAsString(snapshot),
                Map.of("days", entity.getDays(), "leaveType", entity.getLeaveType())));
        for (Long attachmentId : selected) {
            var retained = new LeaveAttachmentSnapshotEntity();
            retained.setLeaveId(entity.getId());
            retained.setInstanceId(instanceId);
            retained.setAttachmentId(attachmentId);
            snapshots.insert(retained);
        }
        entity.setBillStatus("B");
        entity.setCurrentInstanceId(instanceId);
        entity.setLastOutcome(null);
        if (mapper.updateById(entity) != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "请假提交状态冲突");
        return entity.getId();
    }

    void delete(LeaveDeleteForm form) {
        var entity = lock(form.id());
        requireOwner(entity);
        dataScope.requireAllowed(entity, "DELETE");
        requireVersion(entity, form.version());
        if (!"A".equals(entity.getBillStatus()) || entity.getCurrentInstanceId() != null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "只能删除从未提交过的草稿");
        }
        try { attachments.deleteForAggregate(LeaveResourceRegistration.RESOURCE_TYPE, entity.getId().toString()); }
        catch (IOException failure) { throw new BizException(ResultEnum.PERSISTENCE_ERROR, "清理请假附件失败"); }
        entries.delete(new LambdaQueryWrapper<LeaveAttachmentEntryEntity>().eq(LeaveAttachmentEntryEntity::getParentId, entity.getId()));
        if (mapper.deleteById(entity.getId()) != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "请假删除失败");
    }

    private LeaveEntity saveAggregate(LeaveSaveForm form, String action) {
        if (!form.getEndTime().isAfter(form.getStartTime())) throw new BizException(ResultEnum.PARAM_ERROR, "结束时间必须晚于开始时间");
        LeaveEntity entity;
        if (form.getId() == null) {
            entity = new LeaveEntity();
            entity.setClientKey(form.getClientKey());
            entity.setOrgId(currentUser.getOrgId());
            entity.setApplicantId(currentUser.getUserId());
            entity.setNumber(numbers.nextNumber(LeaveNumberReferenceProvider.REFERENCE,
                    NumberGenerationContext.forOrganization(entity.getOrgId(), form.getBizDate())));
            entity.setBillStatus("A");
            entity.setVersion(0);
        } else {
            entity = lock(form.getId());
            requireOwner(entity);
            requireVersion(entity, form.getVersion());
            if (!"A".equals(entity.getBillStatus())) throw new BizException(ResultEnum.PARAM_ERROR, "当前请假单不可编辑");
            if (!Objects.equals(entity.getClientKey(), form.getClientKey())) throw new BizException(ResultEnum.PARAM_ERROR, "单据编辑标识不匹配");
        }
        entity.setBizDate(form.getBizDate());
        entity.setLeaveType(form.getLeaveType());
        entity.setStartTime(form.getStartTime());
        entity.setEndTime(form.getEndTime());
        entity.setDays(form.getDays());
        entity.setReason(form.getReason().trim());
        dataScope.requireAllowed(entity, action);
        int affected = form.getId() == null ? mapper.insert(entity) : mapper.updateById(entity);
        if (affected != 1) throw new BizException(ResultEnum.DATA_CONFLICT, "请假数据已变化");
        promote(form, entity.getId());
        entries.delete(new LambdaQueryWrapper<LeaveAttachmentEntryEntity>().eq(LeaveAttachmentEntryEntity::getParentId, entity.getId()));
        for (Long attachmentId : new LinkedHashSet<>(form.getAttachmentIds())) {
            var entry = new LeaveAttachmentEntryEntity();
            entry.setParentId(entity.getId());
            entry.setAttachmentId(attachmentId);
            entries.insert(entry);
        }
        return entity;
    }

    private void promote(LeaveSaveForm form, Long id) {
        if (form.getAttachmentIds().isEmpty()) return;
        var command = new AttachmentPromoteCommand();
        command.setAttachmentIds(form.getAttachmentIds().stream().distinct().toList());
        command.setUploadSessions(form.getAttachmentUploadSessions());
        command.setBizType(LeaveResourceRegistration.RESOURCE_TYPE);
        command.setBizId(id.toString());
        try { attachments.promoteForAggregate(command); }
        catch (IOException failure) { throw new BizException(ResultEnum.PERSISTENCE_ERROR, "确认请假附件失败"); }
    }
    private String fingerprint(LeaveSubmitForm form) {
        // 以强类型表单和排序后的 Map 计算指纹，JSON 属性顺序不影响同一命令重放。
        var canonical = tools.jackson.databind.json.JsonMapper.builder()
                .enable(tools.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(tools.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).build();
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(canonical.writeValueAsBytes(form)));
        } catch (java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
    }
    private LeaveEntity lock(Long id) {
        var entity = mapper.selectOne(new LambdaQueryWrapper<LeaveEntity>().eq(LeaveEntity::getId, id).last("FOR UPDATE"));
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "请假申请不存在");
        return entity;
    }
    private void requireOwner(LeaveEntity entity) {
        if (!Objects.equals(entity.getApplicantId(), currentUser.getUserId())) throw new BizException(ResultEnum.PERMISSION_ERROR, "只能修改本人的请假申请");
    }
    private void requireVersion(LeaveEntity entity, Integer version) {
        if (!Objects.equals(entity.getVersion(), version)) throw new BizException(ResultEnum.DATA_CONFLICT, "请假申请已变化，请刷新");
    }
}
