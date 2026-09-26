package sm.domain.demo.office.leave.service;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.demo.office.leave.constant.LeavePermission;
import sm.domain.demo.office.leave.mapper.*;
import sm.domain.demo.office.leave.model.entity.*;
import sm.domain.workflow.process.instance.contract.WorkflowHistoryReader;
import sm.system.exception.BizException;
import sm.system.resource.*;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
final class LeaveResourceRegistration implements BusinessResourceRegistration, BusinessResourceAccessPolicy {
    static final String RESOURCE_TYPE = "demo.office.leave";
    static final String BUSINESS_TYPE = "demo/office/leave";
    private final LeaveMapper mapper;
    private final LeaveAttachmentEntryMapper entries;
    private final LeaveAttachmentSnapshotMapper snapshots;
    private final LeaveDataScope dataScope;
    private final WorkflowHistoryReader history;
    private final CurrentUserContext currentUser;
    @Override public String resourceType() { return RESOURCE_TYPE; }
    @Override public BusinessResourceAccessPolicy accessPolicy() { return this; }
    @Override public boolean supportsDataScope() { return true; }
    @Override public Set<String> dataScopeActions() { return Set.of("VIEW", "SAVE", "SUBMIT", "DELETE"); }
    @Override public void requireUploadAllowed() { StpUtil.checkPermission(LeavePermission.SAVE); }

    @Override
    public void requireAllowed(String resourceId, BusinessResourceAction action) {
        var entity = require(resourceId, false);
        if (action == BusinessResourceAction.READ) {
            StpUtil.checkPermission(LeavePermission.DETAIL);
            dataScope.requireAllowed(entity, "VIEW");
        } else {
            requireMutable(entity);
        }
    }

    @Override
    public void requireAttachmentAllowed(String resourceId, Long attachmentId, BusinessResourceAction action) {
        var entity = require(resourceId, false);
        if (action != BusinessResourceAction.READ) {
            requireMutable(entity);
            requireUnretained(entity.getId(), attachmentId);
            return;
        }
        if (StpUtil.hasPermission(LeavePermission.DETAIL) && dataScope.allows(entity, "VIEW")
                && entries.selectCount(new LambdaQueryWrapper<LeaveAttachmentEntryEntity>().eq(LeaveAttachmentEntryEntity::getParentId, entity.getId())
                        .eq(LeaveAttachmentEntryEntity::getAttachmentId, attachmentId)) > 0) return;
        for (var snapshot : snapshots.selectList(new LambdaQueryWrapper<LeaveAttachmentSnapshotEntity>()
                .eq(LeaveAttachmentSnapshotEntity::getLeaveId, entity.getId()).eq(LeaveAttachmentSnapshotEntity::getAttachmentId, attachmentId))) {
            if (history.canRead(BUSINESS_TYPE, entity.getId(), snapshot.getInstanceId())) return;
        }
        throw new BizException(ResultEnum.PERMISSION_ERROR, "无权读取该轮次附件");
    }

    @Override
    public void beforeAttachmentMutation(String resourceId, Long attachmentId, BusinessResourceAction action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("附件维护必须位于事务内");
        var entity = require(resourceId, true);
        requireMutableState(entity);
        requireUnretained(entity.getId(), attachmentId);
    }

    private LeaveEntity require(String resourceId, boolean lock) {
        Long id;
        try { id = Long.valueOf(resourceId); }
        catch (RuntimeException failure) { throw new BizException(ResultEnum.PERMISSION_ERROR, "请假资源标识无效"); }
        var query = new LambdaQueryWrapper<LeaveEntity>().eq(LeaveEntity::getId, id);
        if (lock) query.last("FOR UPDATE");
        var entity = mapper.selectOne(query);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "请假申请不存在");
        return entity;
    }

    private void requireMutable(LeaveEntity entity) {
        StpUtil.checkPermission(LeavePermission.SAVE);
        dataScope.requireAllowed(entity, "SAVE");
        requireMutableState(entity);
    }

    private void requireMutableState(LeaveEntity entity) {
        if (!Objects.equals(entity.getApplicantId(), currentUser.getUserId()) || !"A".equals(entity.getBillStatus())) {
            throw new BizException(ResultEnum.PERMISSION_ERROR, "只有申请人可以维护草稿附件");
        }
    }

    private void requireUnretained(Long leaveId, Long attachmentId) {
        if (snapshots.selectCount(new LambdaQueryWrapper<LeaveAttachmentSnapshotEntity>()
                .eq(LeaveAttachmentSnapshotEntity::getLeaveId, leaveId).eq(LeaveAttachmentSnapshotEntity::getAttachmentId, attachmentId)) > 0) {
            throw new BizException(ResultEnum.PARAM_ERROR, "历史轮次已引用此附件，只能从新草稿中移除选择，不能修改或删除原件");
        }
    }
}
