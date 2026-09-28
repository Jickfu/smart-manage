package sm.domain.workflow.process.instance.model.vo;

import sm.domain.workflow.process.engine.WorkflowEngine;

import java.time.LocalDateTime;

public record InstanceListVO(Long id, String businessType, Long businessId, String number,
                             Long orgId, Long applicantId, String applicantName, WorkflowEngine.State state,
                             boolean active, String currentNode, LocalDateTime createTime) { }
