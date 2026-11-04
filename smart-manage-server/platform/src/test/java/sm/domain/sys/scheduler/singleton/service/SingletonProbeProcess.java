package sm.domain.sys.scheduler.singleton.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import sm.domain.sys.scheduler.singleton.contract.*;

/** 双 JVM 演练入口，仅位于测试产物，不装配到正式发行。 */
public final class SingletonProbeProcess {
    public static void main(String[] arguments) throws Exception {
        var environment = System.getenv();
        var source = new DriverManagerDataSource(environment.get("SM_PROBE_URL"), environment.get("SM_PROBE_USER"), environment.get("SM_PROBE_PASSWORD"));
        var jdbc = new JdbcTemplate(source);
        var transaction = new TransactionTemplate(new DataSourceTransactionManager(source));
        String number = environment.get("SM_PROBE_NUMBER");
        String instance = environment.get("SM_PROBE_INSTANCE");
        String mode = environment.get("SM_PROBE_MODE");
        SingletonTask task = new SingletonTask() {
            @Override public SingletonTaskDefinition definition() {
                return new SingletonTaskDefinition(number, "双实例验证", "scheduler", "测试专用任务", true, true, 8);
            }
            @Override public void run(SingletonTaskContext context) throws Exception {
                if ("failure".equals(mode)) throw new IllegalStateException("模拟失败");
                if ("listener".equals(mode)) {
                    // 常驻监听样板：取消关闭资源唤醒阻塞，退出前完成资源关闭。
                    try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
                        server.setSoTimeout(200);
                        context.onCancel(() -> { try { server.close(); } catch (java.io.IOException ignored) { } });
                        context.ready();
                        while (!context.cancelled()) {
                            try (var socket = server.accept()) { context.checkActive(); }
                            catch (SocketTimeoutException timeout) { /* 超时提供取消与运行代际检查机会。 */ }
                            if (!context.cancelled()) write(context);
                        }
                    }
                } else {
                    // 循环处理样板：每一批先检查取消，业务写入与代际校验在同一事务。
                    context.ready();
                    while (!context.cancelled()) { write(context); Thread.sleep(150); }
                }
            }
            private void write(SingletonTaskContext context) {
                context.checkActive();
                transaction.executeWithoutResult(status -> {
                    context.guardTransaction();
                    jdbc.update("INSERT INTO t_singleton_probe_test(task_number,instance_id,run_id,generation) VALUES (?,?,?,?)",
                            number, instance, context.runId(), context.generation());
                });
            }
        };
        var properties = new org.springframework.boot.jdbc.autoconfigure.DataSourceProperties();
        properties.setUrl(environment.get("SM_PROBE_URL"));
        properties.setUsername(environment.get("SM_PROBE_USER"));
        properties.setPassword(environment.get("SM_PROBE_PASSWORD"));
        var sessions = new sm.system.concurrent.PostgresSessionFactory(properties);
        try (var coordinator = new SingletonTaskCoordinator(new SingletonTaskRegistry(List.of(task)),
                new SingletonTaskAccessor(source), sessions, instance)) {
            coordinator.start();
            System.out.println("PROBE_READY " + instance);
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        }
    }
}
