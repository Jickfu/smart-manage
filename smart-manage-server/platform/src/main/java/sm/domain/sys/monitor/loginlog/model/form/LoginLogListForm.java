package sm.domain.sys.monitor.loginlog.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.domain.sys.monitor.loginlog.constant.LoginEventType;
import sm.system.form.PageForm;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录日志分页查询")
public class LoginLogListForm extends PageForm {
	@Schema(description = "关键词：用户名/昵称")
	private String keyword;
	@Schema(description = "是否成功，空表示全部")
	private Boolean success;
	@Schema(description = "事件类型，空表示全部")
	private LoginEventType eventType;
	@Schema(description = "完整链路追踪ID")
	private String traceId;
	@Schema(description = "开始时间")
	private LocalDateTime beginTime;
	@Schema(description = "结束时间")
	private LocalDateTime endTime;
}

