package sm.domain.workflow.process.instance.model.vo;

import sm.domain.workflow.process.engine.WorkflowEngine;
import sm.domain.workflow.process.runtime.model.vo.WorkflowOperationVO;
import sm.domain.workflow.process.task.model.vo.CandidateChangeVO;

import java.util.List;
import java.util.Map;

public record InstanceAdminDetailVO(Long id, String businessType, Long businessId, String number,
                                    Long orgId, Long applicantId, WorkflowEngine.Run run,
                                    Map<Long, String> actorNames,
                                    List<CandidateChangeVO> candidateChanges,
                                    List<WorkflowOperationVO> operations) { }
