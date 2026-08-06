package sm.system.concurrent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** PostgreSQL 会话级 advisory lock；连接断开时数据库自动释放锁。 */
@Component
@RequiredArgsConstructor
public class PostgresDistributedMutex implements DistributedMutex {
    private final DataSource dataSource;

    @Override
    public LockHandle acquire(String namespace, String key) {
        if (key == null || key.isBlank()) return () -> { };
        String qualifiedKey = namespace + ":" + key;
        long lockId = lockId(qualifiedKey);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            try (PreparedStatement statement = connection.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
                statement.setLong(1, lockId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next() || !resultSet.getBoolean(1)) {
                        connection.close();
                        throw new DistributedMutexBusyException(qualifiedKey);
                    }
                }
            }
            return new AdvisoryLockHandle(connection, lockId);
        } catch (SQLException exception) {
            closeQuietly(connection);
            throw new IllegalStateException("获取分布式互斥锁失败: " + qualifiedKey, exception);
        }
    }

    private long lockId(String key) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getLong();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }

    private static void closeQuietly(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException ignored) {
            // 不覆盖原始异常。
        }
    }

    private static final class AdvisoryLockHandle implements LockHandle {
        private final Connection connection;
        private final long lockId;
        private boolean closed;

        private AdvisoryLockHandle(Connection connection, long lockId) {
            this.connection = connection;
            this.lockId = lockId;
        }

        @Override
        public synchronized void close() {
            if (closed) return;
            closed = true;
            try (PreparedStatement statement = connection.prepareStatement("SELECT pg_advisory_unlock(?)")) {
                statement.setLong(1, lockId);
                statement.execute();
            } catch (SQLException ignored) {
                // 即使显式解锁失败，关闭会话也会释放该锁。
            } finally {
                closeQuietly(connection);
            }
        }
    }
}
