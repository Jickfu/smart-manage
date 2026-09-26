package sm.domain.workflow.process.assignment.model.form;
import jakarta.validation.constraints.Size;
public record AssignmentFeedbackForm(@Size(max = 10000) String storageIds) { }
