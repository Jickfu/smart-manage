package sm.domain.workflow.process.runtime.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sm.domain.workflow.process.runtime.mapper.RuntimeCommandMapper;
import sm.domain.workflow.process.runtime.model.entity.RuntimeCommandEntity;
import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

/** 回执只参加当前命令事务，失败回滚后不留下虚假的成功记录。 */
@Component
@RequiredArgsConstructor
class RuntimeCommandWriter {
    private final RuntimeCommandMapper mapper;
    private final ObjectMapper json;
    WorkflowEngine.Run replay(UUID requestId, Long actorId, String digest) {
        if (requestId == null) throw new BizException(ResultEnum.PARAM_ERROR, "缺少命令请求标识");
        mapper.lockRequest(requestId);
        var receipt = mapper.selectById(requestId);
        if (receipt == null) return null;
        if (!Objects.equals(receipt.getActorId(), actorId) || !Objects.equals(receipt.getRequestDigest(), digest)) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "请求标识已用于不同操作者或命令内容");
        }
        return json.readValue(receipt.getResult(), WorkflowEngine.Run.class);
    }
    void save(UUID requestId, Long actorId, String digest, WorkflowEngine.Run result) {
        var receipt = new RuntimeCommandEntity();
        receipt.setRequestId(requestId); receipt.setActorId(actorId); receipt.setInstanceId(result.id());
        receipt.setRequestDigest(digest); receipt.setResult(json.writeValueAsString(result));
        mapper.insert(receipt);
    }
    String digest(String action, Long instanceId, Long taskId, String opinion) {
        try {
            return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(json.writeValueAsBytes(Arrays.asList(action, instanceId, taskId, opinion))));
        } catch (java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
    }
}
