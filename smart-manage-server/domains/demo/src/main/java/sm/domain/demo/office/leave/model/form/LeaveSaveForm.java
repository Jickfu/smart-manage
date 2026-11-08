package sm.domain.demo.office.leave.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
@Data
@Schema(description = "新增或保存请假暂存单请求")
public class LeaveSaveForm {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "已有请假单的乐观锁版本，新建时为空")
    private Integer version;
    @Schema(description = "客户端生成的请假草稿 UUID")
    @NotNull private UUID clientKey;
    @Schema(description = "业务日期")
    @NotNull private LocalDate bizDate;
    @Schema(description = "请假类型，取值 ANNUAL、SICK、PERSONAL 或 OTHER")
    @NotBlank @Pattern(regexp = "ANNUAL|SICK|PERSONAL|OTHER") private String leaveType;
    @Schema(description = "请假开始时间")
    @NotNull private LocalDateTime startTime;
    @Schema(description = "请假结束时间")
    @NotNull private LocalDateTime endTime;
    @Schema(description = "请假天数，单位为天，最多两位小数")
    @NotNull @DecimalMin("0.01") @Digits(integer = 4, fraction = 2) private BigDecimal days;
    @Schema(description = "请假事由")
    @NotBlank @Size(max = 2000) private String reason;
    @Schema(description = "当前草稿保留的平台附件 ID 列表")
    @NotNull @Size(max = 50) private List<@NotNull @Positive Long> attachmentIds = List.of();
    @Schema(description = "附件上传会话映射，键为附件 ID，值为本次上传会话标识")
    private Map<Long, String> attachmentUploadSessions = Map.of();
}
