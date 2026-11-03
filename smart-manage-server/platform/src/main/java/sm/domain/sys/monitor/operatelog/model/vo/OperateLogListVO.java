package sm.domain.sys.monitor.operatelog.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志列表项")
public class OperateLogListVO implements Serializable {
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "业务名称")
	private String bizName;
	@Schema(description = "是否成功")
	private Boolean success;
	@Schema(description = "错误说明")
	private String errorMsg;
	@Schema(description = "请求方法")
	private String requestMethod;
	@Schema(description = "请求URI")
	private String requestUri;
	@Schema(description = "IP地址")
	private String ip;
	@Schema(description = "类名")
	private String className;
	@Schema(description = "方法名")
	private String methodName;
	@Schema(description = "耗时(ms)")
	private Long durationMs;
	@Schema(description = "用户名")
	private String username;
	@Schema(description = "链路追踪ID")
	private String traceId;
	@Schema(description = "创建时间")
	private LocalDateTime createTime;
}

