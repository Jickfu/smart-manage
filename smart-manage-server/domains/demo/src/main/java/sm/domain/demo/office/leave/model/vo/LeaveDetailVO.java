package sm.domain.demo.office.leave.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.attachment.contract.AttachmentReference;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
@Data
@Schema(description = "请假单详情与附件引用")
public class LeaveDetailVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本，修改已有记录时使用")
    private Integer version;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "客户端生成的请假草稿 UUID")
    private UUID clientKey;
    @Schema(description = "单据所属组织 ID")
    private Long orgId;
    @Schema(description = "申请人用户 ID")
    private Long applicantId;
    @Schema(description = "业务日期")
    private LocalDate bizDate;
    @Schema(description = "请假类型，取值 ANNUAL、SICK、PERSONAL 或 OTHER")
    private String leaveType;
    @Schema(description = "请假开始时间")
    private LocalDateTime startTime;
    @Schema(description = "请假结束时间")
    private LocalDateTime endTime;
    @Schema(description = "请假天数，单位为天，最多两位小数")
    private BigDecimal days;
    @Schema(description = "请假事由")
    private String reason;
    @Schema(description = "单据状态，取值见标准单据状态及请假样板约束")
    private String billStatus;
    @Schema(description = "最近一轮审批的工作流实例 ID，未发起时为空")
    private Long currentInstanceId;
    @Schema(description = "最近一轮审批的结果，未结束时可为空")
    private String lastOutcome;
    @Schema(description = "编辑页明细占位列表，请假单当前无明细行")
    private List<Object> entries = List.of();
    @Schema(description = "当前详情可读取的附件引用列表")
    private List<AttachmentReference> attachments = List.of();
    @Schema(description = "已被历史审批快照引用而须保留的附件 ID 列表")
    private List<Long> retainedAttachmentIds = List.of();
}
