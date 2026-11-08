package sm.domain.sys.monitor.slowsql.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

import java.util.List;

@Schema(description = "慢 SQL 快照输出模型")
@Data
public class SlowSqlSnapshotVO {
    @Schema(description = "应用实例唯一标识")
    private String instanceId;
    @Schema(description = "采样时间")
    private String sampleTime;
    @Schema(description = "慢 SQL 判定阈值，单位毫秒")
    private long thresholdMs;
    @Schema(description = "查询结果记录列表")
    private List<SqlStatVO> records;

    @Schema(description = "慢 SQL 统计条目")
    @Data
    public static class SqlStatVO {
        @Schema(description = "ID")
        private long id;
        @Schema(description = "被监控的 SQL 语句")
        private String sql;
        @Schema(description = "SQL 累计执行次数")
        private long executeCount;
        @Schema(description = "SQL 累计成功执行次数")
        private long executeSuccessCount;
        @Schema(description = "累计错误次数")
        private long errorCount;
        @Schema(description = "SQL 累计执行耗时，单位毫秒")
        private long executeMillisTotal;
        @Schema(description = "SQL 最大执行耗时，单位毫秒")
        private long executeMillisMax;
        @Schema(description = "SQL 平均执行耗时，单位毫秒")
        private double executeMillisAverage;
        @Schema(description = "SQL 最大并发执行数")
        private long concurrentMax;
        @Schema(description = "SQL 在事务内累计执行次数")
        private long inTransactionCount;
        @Schema(description = "SQL 累计更新行数")
        private long updateCount;
        @Schema(description = "SQL 累计读取行数")
        private long fetchRowCount;
        @Schema(description = "最近一次执行时间")
        private String lastExecuteTime;
    }
}
