package sm.domain.workflow.process.definition.model.vo;
import java.util.List;
import sm.domain.workflow.process.engine.WorkflowDefinitions;
public record DefinitionListVO(Long id, String number, String name, String businessType, boolean enabled,
                               Integer version, List<WorkflowDefinitions.Definition> definitions) { }
