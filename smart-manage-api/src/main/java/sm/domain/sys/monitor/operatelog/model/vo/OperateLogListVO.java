package sm.domain.sys.monitor.operatelog.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志列表项")
public class OperateLogListVO implements Serializable {
	private Long id;
	private String bizName;
	private Boolean success;
	private String errorMsg;
	private String requestMethod;
	private String requestUri;
	private String ip;
	private String className;
	private String methodName;
	private Long durationMs;
	private String username;
	private String traceId;
	private LocalDateTime createTime;
}

