package sm.domain.demo.office.leave.model.form;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.UUID;
@Data
@EqualsAndHashCode(callSuper = true)
public class LeaveSubmitForm extends LeaveSaveForm { @NotNull private UUID requestId; }
