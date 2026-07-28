package sm.domain.sys.monitor.operatelog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * 操作日志（BizLog）
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_operate_log")
public class OperateLogEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	private String bizName;
	private Boolean success;
	private String errorMsg;
	private String requestMethod;
	private String requestUri;
	private String ip;
	private String userAgent;
	private String className;
	private String methodName;
	private Long durationMs;
	private String requestParams;
	private String responseBody;
	private Long userId;
	private String username;
	private String traceId;
}
