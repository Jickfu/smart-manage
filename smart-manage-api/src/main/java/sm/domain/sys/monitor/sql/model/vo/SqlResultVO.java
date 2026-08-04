package sm.domain.sys.monitor.sql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * SQL 执行结果
 */
@Data
@Schema(description = "SQL 执行结果")
public class SqlResultVO {

    @Schema(description = "结果类型: QUERY / DML / DDL / ERROR")
    private String type;

    @Schema(description = "列名列表（仅 QUERY 类型）")
    private List<SqlColumnVO> columns;

    @Schema(description = "数据行（仅 QUERY 类型）")
    private List<List<Object>> rows;

    @Schema(description = "影响/返回行数")
    private Integer rowCount;

    @Schema(description = "执行耗时（ms）")
    private Integer executeDuration;

    @Schema(description = "提示/错误消息")
    private String message;

    @Schema(description = "查询结果是否因行数上限被截断")
    private boolean truncated;

    @Schema(description = "本次执行包含的语句数")
    private Integer statementCount;

    @Schema(description = "批量 INSERT 每条语句的影响行数")
    private List<Integer> statementRowCounts;
}
