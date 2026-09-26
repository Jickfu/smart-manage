package sm.domain.workflow.process.assignment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.user.contract.UserAssignmentReader;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

/** 只在节点激活时解析，解析结果由引擎保存为当前任务的候选人快照。 */
@Service
@RequiredArgsConstructor
public class AssignmentService {
    private final UserAssignmentReader assignments;
    private final UserReferenceReader users;

    public List<Long> resolve(String rule, Long orgId) {
        List<Long> candidates;
        if ("sm:leader".equals(rule)) {
            candidates = assignments.findEnabledOrgLeaders(orgId);
        } else if (rule != null && rule.matches("sm:role:[1-9][0-9]{0,18}")) {
            candidates = assignments.findEnabledRoleMembers(orgId, identifier(rule));
        } else if (rule != null && rule.matches("sm:user:[1-9][0-9]{0,18}")) {
            candidates = List.of(users.requireEnabled(identifier(rule)).id());
        } else {
            throw new BizException(ResultEnum.PARAM_ERROR, "不支持的审批人规则");
        }
        if (candidates.isEmpty()) throw new BizException(ResultEnum.PARAM_ERROR, "审批节点没有有效候选人，请维护人员任职或调整流程定义");
        return List.copyOf(candidates);
    }

    private Long identifier(String rule) {
        try { return Long.valueOf(rule.substring(rule.lastIndexOf(':') + 1)); }
        catch (NumberFormatException failure) { throw new BizException(ResultEnum.PARAM_ERROR, "审批人规则标识无效"); }
    }
}
