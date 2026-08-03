package sm.domain.sys.monitor.operatelog.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "操作日志分页查询")
public class OperateLogListForm extends PageForm {
	@Schema(description = "关键词：URI/方法名/业务名")
	private String keyword;
	@Schema(description = "是否成功，空表示全部")
	private Boolean success;
	@Schema(description = "完整链路追踪ID")
	private String traceId;
	@Schema(description = "开始时间")
	private LocalDateTime beginTime;
	@Schema(description = "结束时间")
	private LocalDateTime endTime;
}

