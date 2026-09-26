package sm.domain.workflow.process.assignment.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.user.contract.UserReferenceReader;
import sm.domain.sys.base.role.contract.RoleReferenceReader;
import sm.domain.workflow.process.assignment.model.form.*;
import sm.domain.workflow.process.assignment.model.vo.AssignmentOptionVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AssignmentChoiceService {
    private final UserReferenceReader users;
    private final RoleReferenceReader roles;
    public List<String> types() { return List.of("用户", "组织角色", "组织负责人"); }
    public Map<String, Object> candidates(AssignmentQueryForm form) {
        String keyword = form.getHandlerName() == null || form.getHandlerName().isBlank() ? form.getHandlerCode() : form.getHandlerName();
        List<AssignmentOptionVO> rows;
        long total;
        if ("用户".equals(form.getHandlerType())) {
            var page = users.searchEnabled(keyword, form.getPageNum(), form.getPageSize());
            rows = page.getRecords().stream().map(user -> new AssignmentOptionVO("sm:user:" + user.id(), user.number(), user.name())).toList();
            total = page.getTotal();
        } else if ("组织角色".equals(form.getHandlerType())) {
            var page = roles.search(keyword, form.getPageNum(), form.getPageSize());
            rows = page.getRecords().stream().map(role -> new AssignmentOptionVO("sm:role:" + role.id(), role.number(), role.name())).toList();
            total = page.getTotal();
        } else if ("组织负责人".equals(form.getHandlerType())) {
            rows = List.of(new AssignmentOptionVO("sm:leader", "ORG_LEADER", "单据所属组织的负责人"));
            total = 1;
        } else throw new BizException(ResultEnum.PARAM_ERROR, "未知审批人类型");
        return Map.of("handlerAuths", Map.of("rows", rows, "total", total), "treeSelections", List.of());
    }
    public List<AssignmentOptionVO> feedback(AssignmentFeedbackForm form) {
        if (form.storageIds() == null || form.storageIds().isBlank()) return List.of();
        var rules = List.of(form.storageIds().split(","));
        if (rules.size() > 100) throw new BizException(ResultEnum.PARAM_ERROR, "审批人回显数量过多");
        var userIds = new ArrayList<Long>();
        var roleIds = new ArrayList<Long>();
        for (String rule : rules) {
            if ("sm:leader".equals(rule)) continue;
            if (!rule.matches("sm:(user|role):[1-9][0-9]{0,18}")) throw new BizException(ResultEnum.PARAM_ERROR, "审批人标识无效");
            Long id;
            try { id = Long.valueOf(rule.substring(rule.lastIndexOf(':') + 1)); }
            catch (NumberFormatException failure) { throw new BizException(ResultEnum.PARAM_ERROR, "审批人标识无效"); }
            if (rule.startsWith("sm:user:")) userIds.add(id); else roleIds.add(id);
        }
        var userReferences = users.findByIds(userIds);
        var roleReferences = roles.findByIds(roleIds);
        var result = new ArrayList<AssignmentOptionVO>();
        for (String rule : rules) {
            if ("sm:leader".equals(rule)) { result.add(new AssignmentOptionVO(rule, "ORG_LEADER", "单据所属组织的负责人")); continue; }
            Long id = Long.valueOf(rule.substring(rule.lastIndexOf(':') + 1));
            String name = "已删除的引用";
            String number = id.toString();
            if (rule.startsWith("sm:user:") && userReferences.containsKey(id)) {
                var user = userReferences.get(id); name = user.name(); number = user.number();
            } else if (rule.startsWith("sm:role:") && roleReferences.containsKey(id)) {
                var role = roleReferences.get(id); name = role.name(); number = role.number();
            }
            result.add(new AssignmentOptionVO(rule, number, name));
        }
        return List.copyOf(result);
    }
}
