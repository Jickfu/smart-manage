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
	/**
	 * ID
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	/**
	 * 业务名称
	 */
	private String bizName;
	/**
	 * 是否成功
	 */
	private Boolean success;
	/**
	 * 错误说明
	 */
	private String errorMsg;
	/**
	 * 请求方法
	 */
	private String requestMethod;
	/**
	 * 请求URI
	 */
	private String requestUri;
	/**
	 * IP地址
	 */
	private String ip;
	/**
	 * 客户端 User-Agent
	 */
	private String userAgent;
	/**
	 * 类名
	 */
	private String className;
	/**
	 * 方法名
	 */
	private String methodName;
	/**
	 * 耗时(ms)
	 */
	private Long durationMs;
	/**
	 * 请求参数
	 */
	private String requestParams;
	/**
	 * 响应内容
	 */
	private String responseBody;
	/**
	 * 用户ID
	 */
	private Long userId;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 链路追踪ID
	 */
	private String traceId;
}
