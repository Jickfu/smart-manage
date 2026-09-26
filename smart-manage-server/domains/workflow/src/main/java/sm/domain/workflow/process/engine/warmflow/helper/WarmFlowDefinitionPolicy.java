package sm.domain.workflow.process.engine.warmflow.helper;

import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.core.dto.NodeJson;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.*;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness.FieldType;

/** 一期可执行能力白名单；不能依赖 iframe 隐藏按钮保障服务端安全。 */
final class WarmFlowDefinitionPolicy {
    private WarmFlowDefinitionPolicy() { }

    static void validate(DefJson definition, Map<String, FieldType> fields, boolean publishing) {
        require("CLASSICS".equals(definition.getModelValue()) || "MIMIC".equals(definition.getModelValue()), "未知设计器模型");
        require(blank(definition.getListenerType()) && blank(definition.getListenerPath()) && blank(definition.getFormPath())
                && !"Y".equals(definition.getFormCustom()), "一期不允许监听器、脚本或自定义表单");
        require(definition.getInstance() == null, "定义不能携带运行实例");
        List<NodeJson> nodes = definition.getNodeList();
        require(nodes != null && nodes.size() >= 2 && nodes.size() <= 100, "流程节点数量必须在 2 到 100 之间");
        Map<String, NodeJson> index = new LinkedHashMap<>();
        for (var node : nodes) {
            require(node != null && node.getNodeCode() != null && node.getNodeCode().matches("[A-Za-z0-9_-]{1,100}"), "节点编码无效");
            require(index.putIfAbsent(node.getNodeCode(), node) == null, "节点编码重复");
            require(node.getNodeType() != null && Set.of(0, 1, 2, 3).contains(node.getNodeType()), "一期只支持开始、结束、审批和条件网关");
            require(blank(node.getListenerType()) && blank(node.getListenerPath()) && blank(node.getFormPath())
                    && !"Y".equals(node.getFormCustom()) && blank(node.getAnyNodeSkip()), "节点含未开放的执行能力");
            require(blank(node.getNodeRatio()) || "0".equals(node.getNodeRatio()), "一期仅支持或签");
            if (node.getNodeType() == 1) {
                require(!publishing || !blank(node.getPermissionFlag()), "发布前必须配置审批人");
                if (!blank(node.getPermissionFlag())) {
                    for (String rule : node.getPermissionFlag().split("@@", -1)) {
                        require(rule.matches("sm:(leader|(?:user|role):[1-9][0-9]{0,18})"), "审批人只能选择用户、组织角色或组织负责人");
                    }
                }
            } else require(blank(node.getPermissionFlag()), "只有审批节点可以配置审批人表达式");
        }
        require(nodes.stream().filter(node -> node.getNodeType() == 0).count() == 1, "必须有且仅有一个开始节点");
        require(nodes.stream().filter(node -> node.getNodeType() == 2).count() == 1, "必须有且仅有一个结束节点");
        if (publishing) require(nodes.stream().anyMatch(node -> node.getNodeType() == 1), "流程至少包含一个人工审批节点");
        for (var node : nodes) {
            var skips = node.getSkipList() == null ? List.<org.dromara.warm.flow.core.dto.SkipJson>of() : node.getSkipList();
            require(skips.size() <= 20, "单个节点的分支过多");
            if (publishing) {
                require(node.getNodeType() == 2 ? skips.isEmpty() : !skips.isEmpty(), "节点连线不完整");
                require(node.getNodeType() == 3 || skips.size() <= 1, "只有条件网关可以分支");
                if (node.getNodeType() == 3) require(skips.stream().filter(skip -> blank(skip.getSkipCondition())).count() == 1, "条件网关必须有一条默认分支");
            }
            for (var skip : skips) {
                require(Objects.equals(skip.getNowNodeCode(), node.getNodeCode()) && index.containsKey(skip.getNextNodeCode()), "连线引用未知节点");
                require(blank(skip.getSkipType()) || "PASS".equals(skip.getSkipType()), "一期不支持退回连线");
                if (!blank(skip.getSkipCondition())) {
                    require(node.getNodeType() == 3, "只有条件网关可以配置条件");
                    String expression = skip.getSkipCondition();
                    require(expression.matches("(eq|ne|gt|ge|lt|le)@@[A-Za-z][A-Za-z0-9_]{0,63}\\|[A-Za-z0-9_.-]{1,100}"), "条件只能使用内置比较表达式，禁止脚本");
                    String field = expression.substring(expression.indexOf("@@") + 2, expression.indexOf('|'));
                    require(fields.containsKey(field), "条件引用未开放的单据字段");
                    WarmFlowConditionPolicy.validate(expression, fields.get(field));
                }
            }
            if (publishing && node.getNodeType() == 3) WarmFlowConditionPolicy.requireDisjoint(skips);
        }
        if (publishing) {
            Set<String> visited = new HashSet<>();
            visit(nodes.stream().filter(node -> node.getNodeType() == 0).findFirst().orElseThrow().getNodeCode(), index, visited, new HashSet<>());
            require(visited.size() == nodes.size(), "存在无法到达的节点");
        }
    }

    private static void visit(String code, Map<String, NodeJson> index, Set<String> visited, Set<String> path) {
        require(path.add(code), "一期不支持循环流程");
        if (visited.add(code)) {
            var skips = index.get(code).getSkipList();
            if (skips != null) for (var skip : skips) visit(skip.getNextNodeCode(), index, visited, path);
        }
        path.remove(code);
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static void require(boolean condition, String message) {
        if (!condition) throw new BizException(ResultEnum.PARAM_ERROR, message);
    }
}
