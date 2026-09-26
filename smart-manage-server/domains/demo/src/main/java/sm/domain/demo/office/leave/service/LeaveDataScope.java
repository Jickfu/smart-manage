package sm.domain.demo.office.leave.service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.demo.office.leave.model.entity.LeaveEntity;
import sm.system.datascope.DataScopeResolver;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Component
@RequiredArgsConstructor
final class LeaveDataScope {
    private final DataScopeResolver resolver;
    void apply(LambdaQueryWrapper<LeaveEntity> query, String action) {
        var scope = resolver.resolve(LeaveResourceRegistration.RESOURCE_TYPE, action);
        if (scope.all()) return;
        query.and(condition -> {
            condition.eq(LeaveEntity::getId, -1L);
            if (scope.selfIncluded()) condition.or().eq(LeaveEntity::getApplicantId, scope.currentUserId());
            if (!scope.orgIds().isEmpty()) condition.or().in(LeaveEntity::getOrgId, scope.orgIds());
        });
    }
    boolean allows(LeaveEntity entity, String action) {
        var scope = resolver.resolve(LeaveResourceRegistration.RESOURCE_TYPE, action);
        return scope.all() || scope.orgIds().contains(entity.getOrgId())
                || scope.selfIncluded() && scope.currentUserId().equals(entity.getApplicantId());
    }
    void requireAllowed(LeaveEntity entity, String action) {
        if (!allows(entity, action)) throw new BizException(ResultEnum.PERMISSION_ERROR, "无权访问该请假申请");
    }
}
