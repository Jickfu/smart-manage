package sm.system.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Component;

/** 常驻协调使用物理连接；close 必须结束会话，不能只是把持锁连接归还普通连接池。 */
@Component
public class PostgresSessionFactory {
    private final SimpleDriverDataSource source;
    public PostgresSessionFactory(DataSourceProperties properties) {
        source = properties.initializeDataSourceBuilder().type(SimpleDriverDataSource.class).build();
        // PostgreSQL 的 URL 参数优先于 Properties，同名 URL 参数取最后一个值。
        // 在 URL 末尾固定协调连接边界，避免业务 URL 的无限等待配置阻断失权取消。
        String url = source.getUrl();
        source.setUrl(url + (url.contains("?") ? "&" : "?")
                + "connectTimeout=3&socketTimeout=5&tcpKeepAlive=true");
    }
    public Connection open() throws SQLException { return source.getConnection(); }
}
