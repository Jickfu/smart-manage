package sm.domain.sys.monitor.slowsql.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class SlowSqlSnapshotVO {
    private String instanceId;
    private String sampleTime;
    private long thresholdMs;
    private List<SqlStatVO> records;

    @Data
    public static class SqlStatVO {
        private long id;
        private String sql;
        private long executeCount;
        private long executeSuccessCount;
        private long errorCount;
        private long executeMillisTotal;
        private long executeMillisMax;
        private double executeMillisAverage;
        private long concurrentMax;
        private long inTransactionCount;
        private long updateCount;
        private long fetchRowCount;
        private String lastExecuteTime;
    }
}
