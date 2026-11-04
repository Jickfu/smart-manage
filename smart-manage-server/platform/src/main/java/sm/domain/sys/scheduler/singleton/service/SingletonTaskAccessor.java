package sm.domain.sys.scheduler.singleton.service;

import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import sm.domain.sys.scheduler.singleton.contract.SingletonTaskDefinition;
import sm.domain.sys.scheduler.singleton.model.entity.SingletonTaskEntity;
import sm.domain.sys.scheduler.singleton.model.vo.SingletonEventVO;

/** 共享状态访问；执行者的状态写入使用实际持锁会话，管理命令使用普通事务连接。 */
@Component
public class SingletonTaskAccessor {
    private static final String OBSERVED_COLUMNS = """
        id,number,name,app_number,description,controllable,requested,version,owner_instance_id,run_id,
        generation,started_at,heartbeat_at,retry_at,last_error,
        CASE WHEN owner_instance_id IS NOT NULL AND (heartbeat_at IS NULL OR heartbeat_at < localtimestamp - interval '15 seconds')
             THEN 'UNKNOWN' ELSE state END AS state
        """;
    private final JdbcTemplate jdbc;
    public SingletonTaskAccessor(DataSource dataSource) {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.setQueryTimeout(3);
    }
    static JdbcTemplate session(Connection connection) {
        JdbcTemplate template = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        template.setQueryTimeout(3);
        return template;
    }
    public void register(SingletonTaskDefinition definition) {
        jdbc.update("""
            INSERT INTO t_sys_singleton_task(id,number,name,app_number,description,controllable,requested)
            VALUES (?,?,?,?,?,?,?) ON CONFLICT (number) DO NOTHING
            """, com.baomidou.mybatisplus.core.toolkit.IdWorker.getId(), definition.number(), definition.name(), definition.appNumber(), definition.description(),
                definition.controllable(), definition.autoStart());
        // 同一编码的发布元数据不可由不同版本节点来回覆盖；变更须随迁移显式交付。
        SingletonTaskEntity stored = find(definition.number());
        if (!stored.getName().equals(definition.name()) || !stored.getAppNumber().equals(definition.appNumber())
                || !stored.getDescription().equals(definition.description())
                || stored.getControllable() != definition.controllable()) {
            throw new IllegalStateException("单例任务元数据与持久定义不一致，请检查发布迁移");
        }
    }
    public SingletonTaskEntity find(String number) { return find(jdbc, number); }
    static SingletonTaskEntity find(JdbcTemplate source, String number) {
        List<SingletonTaskEntity> rows = source.query("SELECT " + OBSERVED_COLUMNS + " FROM t_sys_singleton_task WHERE number=?",
                BeanPropertyRowMapper.newInstance(SingletonTaskEntity.class), number);
        return rows.isEmpty() ? null : rows.getFirst();
    }
    public List<SingletonTaskEntity> list(String keyword, int offset, int limit) {
        String search = keyword == null ? "" : keyword;
        return jdbc.query("SELECT " + OBSERVED_COLUMNS + """
            FROM t_sys_singleton_task WHERE strpos(number,?)>0 OR strpos(name,?)>0
            ORDER BY number LIMIT ? OFFSET ?
            """, BeanPropertyRowMapper.newInstance(SingletonTaskEntity.class), search, search, limit, offset);
    }
    public long count(String keyword) {
        String search = keyword == null ? "" : keyword;
        return jdbc.queryForObject("SELECT count(*) FROM t_sys_singleton_task WHERE strpos(number,?)>0 OR strpos(name,?)>0",
                Long.class, search, search);
    }
    public int control(String number, int version, boolean requested) {
        int changed = jdbc.update("""
            UPDATE t_sys_singleton_task SET requested=?,version=version+1
            WHERE number=? AND version=? AND controllable
              AND (NOT ? OR (owner_instance_id IS NULL AND state IN ('STOPPED','FAILED')))
            """, requested, number, version, requested);
        if (changed == 1) event(jdbc, number, requested ? "START_REQUESTED" : "STOP_REQUESTED", null, null, null);
        return changed;
    }
    public List<SingletonEventVO> events(String number) {
        return jdbc.query("""
            SELECT id,occurred_at,event_type,instance_id,run_id,reason FROM t_sys_singleton_event
            WHERE task_number=? ORDER BY id DESC LIMIT 200
            """, (row, rowNumber) -> new SingletonEventVO(row.getLong(1), row.getTimestamp(2).toLocalDateTime(),
                row.getString(3), row.getString(4), row.getString(5), row.getString(6)), number);
    }
    static void event(JdbcTemplate source, String number, String type, String instance, String runId, String reason) {
        source.update("INSERT INTO t_sys_singleton_event(task_number,event_type,instance_id,run_id,reason) VALUES (?,?,?,?,?)",
                number, type, instance, runId, reason);
        source.update("""
            DELETE FROM t_sys_singleton_event WHERE task_number=? AND id NOT IN
            (SELECT id FROM t_sys_singleton_event WHERE task_number=? ORDER BY id DESC LIMIT 200)
            """, number, number);
    }
    public void guard(String number, String runId, long generation) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.hasResource(java.util.Objects.requireNonNull(jdbc.getDataSource()))) {
            throw new IllegalStateException("运行代际校验必须位于业务写入事务内");
        }
        // 锁住同一运行行直到业务提交，接管更新必须等待，消除先校验后写入的竞态。
        var rows = jdbc.queryForList("""
            SELECT number FROM t_sys_singleton_task
            WHERE number=? AND run_id=? AND generation=? AND requested AND state IN ('STARTING','RUNNING')
              AND heartbeat_at >= localtimestamp - interval '15 seconds' FOR UPDATE
            """, number, runId, generation);
        if (rows.isEmpty()) throw new IllegalStateException("单例任务执行权已经失效");
    }
}
