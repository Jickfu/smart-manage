package sm.domain.sys.scheduler.singleton.service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;

/** 持锁会话和运行状态更新共用物理连接，数据库会话失效后不能用新连接延续旧执行权。 */
final class SingletonLease implements AutoCloseable {
    private final Connection connection;
    private final JdbcTemplate jdbc;
    private final String number;
    private final String instanceId;
    private String runId;
    private long generation;

    private SingletonLease(Connection connection, String number, String instanceId) {
        this.connection = connection;
        this.jdbc = SingletonTaskAccessor.session(connection);
        this.number = number;
        this.instanceId = instanceId;
    }
    static SingletonLease acquire(Connection connection, String number, String instanceId) throws Exception {
        SingletonLease lease = new SingletonLease(connection, number, instanceId);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(("smart-manage:singleton:" + number).getBytes(StandardCharsets.UTF_8));
            if (!Boolean.TRUE.equals(lease.jdbc.queryForObject("SELECT pg_try_advisory_lock(?)", Boolean.class,
                    ByteBuffer.wrap(digest).getLong()))) {
                lease.close();
                return null;
            }
            return lease;
        } catch (Exception failure) {
            lease.close();
            throw failure;
        }
    }
    long generation() { return generation; }
    boolean claim(String nextRunId) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String previousRun = jdbc.queryForObject("SELECT run_id FROM t_sys_singleton_task WHERE number=? FOR UPDATE", String.class, number);
            var generations = jdbc.queryForList("""
                UPDATE t_sys_singleton_task SET owner_instance_id=?,run_id=?,generation=generation+1,
                state='STARTING',started_at=localtimestamp,heartbeat_at=localtimestamp,last_error=NULL,retry_at=NULL
                WHERE number=? AND requested AND (retry_at IS NULL OR retry_at <= localtimestamp)
                RETURNING generation
                """, Long.class, instanceId, nextRunId, number);
            if (generations.isEmpty()) {
                // 只有取得实际锁的节点才能结束异常退出留下的停止请求。
                int stopped = jdbc.update("""
                    UPDATE t_sys_singleton_task SET state='STOPPED',owner_instance_id=NULL,run_id=NULL,
                    heartbeat_at=localtimestamp,retry_at=NULL WHERE number=? AND NOT requested
                    AND (state <> 'STOPPED' OR owner_instance_id IS NOT NULL)
                    """, number);
                if (stopped == 1) SingletonTaskAccessor.event(jdbc, number, "STOPPED", instanceId, null, "RECOVERED_STOP");
                connection.commit();
                return false;
            }
            generation = generations.getFirst();
            runId = nextRunId;
            SingletonTaskAccessor.event(jdbc, number, "ACQUIRED", instanceId, runId, previousRun == null ? null : "PREVIOUS_RUN_LOST");
            connection.commit();
            return true;
        } catch (RuntimeException | SQLException failure) {
            connection.rollback();
            throw failure;
        } finally { connection.setAutoCommit(true); }
    }
    boolean heartbeat(String state) throws SQLException {
        connection.setAutoCommit(false);
        try {
            String previous = jdbc.queryForObject("SELECT state FROM t_sys_singleton_task WHERE number=? FOR UPDATE", String.class, number);
            var requested = jdbc.queryForList("""
                UPDATE t_sys_singleton_task SET heartbeat_at=localtimestamp,state=?
                WHERE number=? AND run_id=? AND generation=? RETURNING requested
                """, Boolean.class, state, number, runId, generation);
            if (requested.size() != 1) throw new IllegalStateException("单例任务所有权状态不匹配");
            if (!state.equals(previous)) SingletonTaskAccessor.event(jdbc, number, state, instanceId, runId, null);
            connection.commit();
            return requested.getFirst();
        } catch (RuntimeException | SQLException failure) {
            connection.rollback();
            throw failure;
        } finally { connection.setAutoCommit(true); }
    }
    void finish(boolean failed, int retrySeconds, String reason) throws SQLException {
        connection.setAutoCommit(false);
        try {
            var states = jdbc.queryForList("""
                UPDATE t_sys_singleton_task SET state=CASE WHEN ? AND requested THEN 'FAILED' ELSE 'STOPPED' END,
                owner_instance_id=NULL,run_id=NULL,heartbeat_at=localtimestamp,
                retry_at=CASE WHEN ? AND requested THEN localtimestamp + (? * interval '1 second') ELSE NULL END,
                last_error=? WHERE number=? AND run_id=? AND generation=? RETURNING state
                """, String.class, failed, failed, retrySeconds, reason, number, runId, generation);
            if (states.size() != 1) throw new IllegalStateException("旧执行者不能提交结束状态");
            // 管理员停止可能与任务失败并发，事件必须使用同一更新实际提交的状态。
            SingletonTaskAccessor.event(jdbc, number, states.getFirst(), instanceId, runId, reason);
            connection.commit();
        } catch (RuntimeException | SQLException failure) {
            connection.rollback();
            throw failure;
        } finally { connection.setAutoCommit(true); }
    }
    @Override public void close() {
        try { connection.close(); }
        catch (SQLException failure) {
            org.slf4j.LoggerFactory.getLogger(SingletonLease.class)
                    .warn("单例任务物理会话关闭失败，task={}", number);
        }
    }
}
