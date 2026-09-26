package sm.domain.workflow.process.engine.warmflow.helper;

import lombok.RequiredArgsConstructor;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.dto.DefJson;
import org.dromara.warm.flow.core.dto.NodeJson;
import org.dromara.warm.flow.core.dto.SkipJson;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.workflow.process.engine.WorkflowDefinitions;
import sm.domain.workflow.process.engine.warmflow.mapper.WarmFlowCoordinationMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import sm.domain.workflow.process.runtime.contract.WorkflowBusiness.FieldType;

@Component
@RequiredArgsConstructor
public class WarmFlowDefinitionAdapter implements WorkflowDefinitions {
    private final WarmFlowCoordinationMapper coordination;

    @Override
    public Definition describe(Long id) {
        var definition = require(id);
        return new Definition(id, definition.getFlowCode(), definition.getFlowName(), definition.getVersion(), !Objects.equals(definition.getIsPublish(), 0));
    }

    @Override
    public List<Definition> list(String code) {
        return FlowEngine.defService().getByFlowCode(code).stream().map(definition -> new Definition(definition.getId(),
                definition.getFlowCode(), definition.getFlowName(), definition.getVersion(), !Objects.equals(definition.getIsPublish(), 0))).toList();
    }

    @Override
    public Long create(String code, String name) {
        lock(code);
        if (!list(code).isEmpty()) throw new BizException(ResultEnum.PARAM_ERROR, "流程编码已存在，请复制版本");
        var definition = new DefJson().setFlowCode(code).setFlowName(name).setModelValue("CLASSICS").setVersion("1")
                    // 官方画布协议的竖线后为文字坐标，省略会使初始节点标签变成 NaN。
                    .setNodeList(List.of(new NodeJson().setNodeCode("start").setNodeName("开始").setNodeType(0).setNodeRatio("0").setCoordinate("200,200|200,240")
                                .setSkipList(List.of(new SkipJson().setNowNodeCode("start").setNextNodeCode("review").setSkipType("PASS"))),
                        new NodeJson().setNodeCode("review").setNodeName("审批").setNodeType(1).setNodeRatio("0").setCoordinate("400,200|400,200")
                                .setSkipList(List.of(new SkipJson().setNowNodeCode("review").setNextNodeCode("end").setSkipType("PASS"))),
                        new NodeJson().setNodeCode("end").setNodeName("结束").setNodeType(2).setNodeRatio("0").setCoordinate("600,200|600,240")));
        return FlowEngine.defService().importDef(definition).getId();
    }

    @Override
    public Design design(Long id) {
        require(id);
        DefJson definition = FlowEngine.defService().queryDesign(id);
        // 摘要须独立于数据库返回顺序，防止没有修改也误报冲突。
        definition.getNodeList().sort(Comparator.comparing(NodeJson::getNodeCode));
        for (var node : definition.getNodeList()) {
            if (node.getSkipList() != null) node.getSkipList().sort(Comparator.comparing(SkipJson::getNextNodeCode));
        }
        String json = FlowEngine.jsonConvert.objToStr(definition);
        return new Design(json, digest(json));
    }

    @Override
    public void save(Long id, String json, String expectedDigest, Map<String, FieldType> fields) {
        var current = require(id);
        lock(current.getFlowCode());
        current = require(id);
        editable(id, current.getIsPublish());
        checkDigest(id, expectedDigest);
        if (json == null || json.length() > 1_000_000) throw new BizException(ResultEnum.PARAM_ERROR, "流程设计内容过大或为空");
        DefJson definition;
        try { definition = FlowEngine.jsonConvert.strToBean(json, DefJson.class); }
        catch (RuntimeException failure) { throw new BizException(ResultEnum.PARAM_ERROR, "流程 JSON 格式无效"); }
        if (definition == null || (definition.getId() != null && !Objects.equals(definition.getId(), id))
                || !Objects.equals(definition.getFlowCode(), current.getFlowCode())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "流程定义身份不匹配");
        }
        // 上游 queryDesign/export 协议不携带 id；保存目标由已授权的请求参数决定。
        // 如客户端显式提供 id 则必须一致，绝不允许利用 saveDef 的空 id 分支新增定义。
        definition.setId(id).setVersion(current.getVersion()).setIsPublish(current.getIsPublish());
        WarmFlowDefinitionPolicy.validate(definition, fields, false);
        // 只替换节点与连线，定义身份、发布状态和业务绑定不由设计器写入。
        try { FlowEngine.defService().saveDef(definition, true); }
        catch (Exception failure) { throw new BizException(ResultEnum.PARAM_ERROR, "流程结构校验失败，请检查节点和连线"); }
    }

    @Override
    public void publish(Long id, String expectedDigest, Map<String, FieldType> fields) {
        var current = require(id);
        lock(current.getFlowCode());
        editable(id, require(id).getIsPublish());
        checkDigest(id, expectedDigest);
        WarmFlowDefinitionPolicy.validate(FlowEngine.defService().queryDesign(id), fields, true);
        // 上游会把未使用的旧发布版本退回草稿；本项目所有发布过的版本均须保持不可变。
        var previousPublished = list(current.getFlowCode()).stream()
                .filter(definition -> definition.published() && !Objects.equals(definition.id(), id))
                .map(Definition::id).toList();
        FlowEngine.defService().publish(id);
        if (!previousPublished.isEmpty()) FlowEngine.defService().updatePublishStatus(previousPublished, 9);
    }

    @Override
    public Long copy(Long id) {
        var current = require(id);
        lock(current.getFlowCode());
        Set<Long> existing = new HashSet<>();
        for (var definition : list(current.getFlowCode())) existing.add(definition.id());
        FlowEngine.defService().copyDef(id);
        Long copied = list(current.getFlowCode()).stream().map(Definition::id).filter(candidate -> !existing.contains(candidate)).findFirst().orElseThrow();
        // 上游 copy 保留源发布标志；本项目新版本必须从未发布草稿开始。
        FlowEngine.defService().updatePublishStatus(List.of(copied), 0);
        return copied;
    }

    private void lock(String code) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) throw new IllegalStateException("流程定义写操作必须参与业务事务");
        coordination.lockDefinitionCode(code);
    }

    private org.dromara.warm.flow.core.entity.Definition require(Long id) {
        var definition = id == null ? null : FlowEngine.defService().getById(id);
        if (definition == null) throw new BizException(ResultEnum.NOT_FOUND, "流程定义不存在");
        return definition;
    }

    private void editable(Long id, Integer published) {
        if (!Objects.equals(published, 0) || !FlowEngine.insService().getByDefId(id).isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "已发布或使用的版本不可修改，请复制新版本");
        }
    }

    private void checkDigest(Long id, String expected) {
        if (!Objects.equals(design(id).digest(), expected)) throw new BizException(ResultEnum.PARAM_ERROR, "流程设计已被修改，请重新打开后编辑");
    }

    public static String digest(String json) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
    }
}
