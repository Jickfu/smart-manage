package sm.domain.sys.monitor.sql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SQL 执行日志列表 VO
 *
 * @author Chekfu
 */
@Data
@Schema(description = "SQL执行日志列表项")
public class SqlLogListVO {

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "SQL文本")
	private String sqlText;

	@Schema(description = "执行耗时(ms)")
	private Integer executeDuration;

	@Schema(description = "结果类型")
	private String resultType;

	@Schema(description = "行数")
	private Integer rowCount;

	@Schema(description = "执行人")
	private String createName;

	@Schema(description = "执行IP")
	private String createIp;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;
}
