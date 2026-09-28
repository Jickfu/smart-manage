package sm.domain.workflow.process.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.workflow.process.task.mapper.TaskCandidateChangeMapper;
import sm.domain.workflow.process.task.model.entity.TaskCandidateChangeEntity;
import sm.domain.workflow.process.task.model.vo.CandidateChangeVO;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/** 提供任务候选人变更的只读投影，避免实例模块直接访问任务模块 Mapper。 */
@Service
@RequiredArgsConstructor
public class TaskCandidateHistoryService {
    private final TaskCandidateChangeMapper changes;
    private final ObjectMapper json;

    public List<CandidateChangeVO> listByInstance(Long instanceId) {
        return changes.selectList(new LambdaQueryWrapper<TaskCandidateChangeEntity>()
                        .eq(TaskCandidateChangeEntity::getInstanceId, instanceId)
                        .orderByAsc(TaskCandidateChangeEntity::getCreateTime))
                .stream().map(change -> new CandidateChangeVO(change.getId(), change.getTaskId(),
                        change.getOperatorId(), ids(change.getBeforeCandidates()), ids(change.getAfterCandidates()),
                        change.getReason(), change.getCreateTime())).toList();
    }

    private List<Long> ids(String value) {
        return json.readValue(value, json.getTypeFactory().constructCollectionType(List.class, Long.class));
    }
}
