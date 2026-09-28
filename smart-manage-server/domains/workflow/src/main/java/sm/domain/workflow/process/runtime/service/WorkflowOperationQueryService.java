package sm.domain.workflow.process.runtime.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.workflow.process.runtime.mapper.WorkflowOperationMapper;
import sm.domain.workflow.process.runtime.model.entity.WorkflowOperationEntity;
import sm.domain.workflow.process.runtime.model.vo.WorkflowOperationVO;

import java.util.List;

/** 提供运行时管理操作的只读投影，避免实例模块直接访问运行时 Mapper。 */
@Service
@RequiredArgsConstructor
public class WorkflowOperationQueryService {
    private final WorkflowOperationMapper operations;

    public List<WorkflowOperationVO> listByInstance(Long instanceId) {
        return operations.selectList(new LambdaQueryWrapper<WorkflowOperationEntity>()
                        .eq(WorkflowOperationEntity::getInstanceId, instanceId)
                        .orderByAsc(WorkflowOperationEntity::getCreateTime))
                .stream().map(operation -> new WorkflowOperationVO(operation.getId(), operation.getTaskId(),
                        operation.getAction(), operation.getOperatorId(), operation.getReason(), operation.getCreateTime()))
                .toList();
    }
}
