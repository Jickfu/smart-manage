package sm.domain.sys.monitor.alert.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

/** 使用真实 partial unique index 验证多实例同时触发的收敛语义。 */
@EnabledIfSystemProperty(named = "smartManage.postgresIntegration", matches = "true")
class MonitorAlertConcurrencyPostgresTests {
  private static final String SCOPE_ID = "verify-concurrent-host";
  private JdbcTemplate jdbcTemplate;
  private TransactionTemplate transactionTemplate;
  private long ruleId;

  @BeforeEach
  void setUp() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource(
            System.getProperty("smartManage.testDbUrl"),
            System.getProperty("smartManage.testDbUser"),
            System.getProperty("smartManage.testDbPassword"));
    jdbcTemplate = new JdbcTemplate(dataSource);
    transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    ruleId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM t_sys_monitor_alert_rule WHERE rule_code='HOST_CPU_HIGH'", Long.class);
  }

  @AfterEach
  void cleanUp() {
    jdbcTemplate.update(
        "DELETE FROM t_sys_monitor_alert_notification WHERE incident_id IN (SELECT id FROM"
            + " t_sys_monitor_alert_incident WHERE scope_id=?)",
        SCOPE_ID);
    jdbcTemplate.update("DELETE FROM t_sys_monitor_alert_incident WHERE scope_id=?", SCOPE_ID);
    jdbcTemplate.update("DELETE FROM t_sys_monitor_instance WHERE instance_id=?", SCOPE_ID);
    jdbcTemplate.update("UPDATE t_sys_monitor_alert_rule SET enabled=true WHERE id=?", ruleId);
  }

  @Test
  void concurrentFirstFiringCreatesOneActiveIncidentAndOneNotification() throws Exception {
    MonitorAlertEvaluation evaluation =
        new MonitorAlertEvaluation(
            ruleId,
            "HOST_CPU_HIGH",
            "HOST",
            SCOPE_ID,
            BigDecimal.valueOf(.95),
            BigDecimal.valueOf(.9),
            true,
            0,
            BigDecimal.valueOf(.8),
            1800,
            true,
            "并发告警");
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var first = executor.submit(() -> evaluateAfterBarrier(evaluation, ready, start));
      var second = executor.submit(() -> evaluateAfterBarrier(evaluation, ready, start));
      ready.await(5, TimeUnit.SECONDS);
      start.countDown();
      first.get(10, TimeUnit.SECONDS);
      second.get(10, TimeUnit.SECONDS);
    }

    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_incident WHERE rule_id=? AND scope_id=? AND"
                + " status IN ('PENDING','FIRING')",
            Integer.class,
            ruleId,
            SCOPE_ID));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_notification notification JOIN"
                + " t_sys_monitor_alert_incident incident ON incident.id=notification.incident_id"
                + " WHERE incident.rule_id=? AND incident.scope_id=? AND"
                + " notification.notification_type='FIRING'",
            Integer.class,
            ruleId,
            SCOPE_ID));
  }

  @Test
  void closedRetirementCycleStaysClosedAndNextFaultCreatesNewCycle() {
    long instanceRuleId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM t_sys_monitor_alert_rule WHERE rule_code='INSTANCE_HEAP_HIGH'",
            Long.class);
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_instance
(id,instance_id,host_id,application_name,first_seen_time,last_seen_time,last_start_time,lifecycle)
VALUES(-9100002,?,?,?,now(),now(),now(),'ACTIVE')
""",
        SCOPE_ID,
        "verify-host",
        "verify-app");
    jdbcTemplate.update(
        """
        INSERT INTO t_sys_monitor_alert_incident
        (id,rule_id,rule_code,scope_type,scope_id,status,cycle_key,started_at,last_evaluated_at,
         last_value,peak_value,threshold,notification_count,summary,version,close_reason)
        VALUES(-9100001,?,'INSTANCE_HEAP_HIGH','INSTANCE',?,'CLOSED','retired-cycle',now(),now(),
               .95,.95,.9,0,'已退役','0','INSTANCE_RETIRED')
        """,
        instanceRuleId,
        SCOPE_ID);
    MonitorAlertEvaluation nextFault =
        new MonitorAlertEvaluation(
            instanceRuleId,
            "INSTANCE_HEAP_HIGH",
            "INSTANCE",
            SCOPE_ID,
            BigDecimal.valueOf(.96),
            BigDecimal.valueOf(.9),
            true,
            0,
            BigDecimal.valueOf(.8),
            1800,
            false,
            "重新激活后的新异常");

    transactionTemplate.executeWithoutResult(
        status -> new MonitorAlertStateTxService(jdbcTemplate).evaluate(nextFault));

    assertEquals(
        2,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_incident WHERE rule_id=? AND scope_id=?",
            Integer.class,
            instanceRuleId,
            SCOPE_ID));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_incident WHERE rule_id=? AND scope_id=? AND"
                + " status='CLOSED' AND close_reason='INSTANCE_RETIRED'",
            Integer.class,
            instanceRuleId,
            SCOPE_ID));
    assertEquals(
        1,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_incident WHERE rule_id=? AND scope_id=? AND"
                + " status='FIRING' AND cycle_key<>'retired-cycle'",
            Integer.class,
            instanceRuleId,
            SCOPE_ID));
  }

  @Test
  void disableWinsAgainstAnEvaluationWaitingOnTheRuleLock() throws Exception {
    MonitorAlertEvaluation evaluation =
        new MonitorAlertEvaluation(
            ruleId,
            "HOST_CPU_HIGH",
            "HOST",
            SCOPE_ID,
            BigDecimal.valueOf(.95),
            BigDecimal.valueOf(.9),
            true,
            0,
            BigDecimal.valueOf(.8),
            1800,
            false,
            "停用并发");
    CountDownLatch disabledButUncommitted = new CountDownLatch(1);
    CountDownLatch evaluationStarted = new CountDownLatch(1);
    CountDownLatch allowCommit = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var disable =
          executor.submit(
              () ->
                  transactionTemplate.executeWithoutResult(
                      status -> {
                        jdbcTemplate.update(
                            "UPDATE t_sys_monitor_alert_rule SET enabled=false WHERE id=?", ruleId);
                        disabledButUncommitted.countDown();
                        await(allowCommit);
                      }));
      disabledButUncommitted.await(5, TimeUnit.SECONDS);
      var evaluate =
          executor.submit(
              () -> {
                evaluationStarted.countDown();
                transactionTemplate.executeWithoutResult(
                    status -> new MonitorAlertStateTxService(jdbcTemplate).evaluate(evaluation));
              });
      evaluationStarted.await(5, TimeUnit.SECONDS);
      allowCommit.countDown();
      disable.get(10, TimeUnit.SECONDS);
      evaluate.get(10, TimeUnit.SECONDS);
    }
    assertEquals(
        0,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_incident WHERE rule_id=? AND scope_id=? AND"
                + " status IN ('PENDING','FIRING')",
            Integer.class,
            ruleId,
            SCOPE_ID));
  }

  @Test
  void retirementWinsAgainstOfflineEvaluationWaitingOnTheInstanceLock() throws Exception {
    long offlineRuleId =
        jdbcTemplate.queryForObject(
            "SELECT id FROM t_sys_monitor_alert_rule WHERE rule_code='INSTANCE_OFFLINE'",
            Long.class);
    jdbcTemplate.update(
        """
INSERT INTO t_sys_monitor_instance
(id,instance_id,host_id,application_name,first_seen_time,last_seen_time,last_start_time,lifecycle)
VALUES(-9200001,?,?,?,now(),now(),now(),'ACTIVE')
""",
        SCOPE_ID,
        "verify-host",
        "verify-app");
    MonitorAlertEvaluation evaluation =
        new MonitorAlertEvaluation(
            offlineRuleId,
            "INSTANCE_OFFLINE",
            "INSTANCE",
            SCOPE_ID,
            BigDecimal.ONE,
            BigDecimal.ONE,
            true,
            0,
            BigDecimal.ZERO,
            1800,
            false,
            "退役并发");
    CountDownLatch retiredButUncommitted = new CountDownLatch(1);
    CountDownLatch evaluationStarted = new CountDownLatch(1);
    CountDownLatch allowCommit = new CountDownLatch(1);
    try (var executor = Executors.newFixedThreadPool(2)) {
      var retire =
          executor.submit(
              () ->
                  transactionTemplate.executeWithoutResult(
                      status -> {
                        jdbcTemplate.update(
                            "UPDATE t_sys_monitor_instance SET lifecycle='RETIRED',retired_at=now()"
                                + " WHERE instance_id=?",
                            SCOPE_ID);
                        retiredButUncommitted.countDown();
                        await(allowCommit);
                      }));
      retiredButUncommitted.await(5, TimeUnit.SECONDS);
      var evaluate =
          executor.submit(
              () -> {
                evaluationStarted.countDown();
                transactionTemplate.executeWithoutResult(
                    status -> new MonitorAlertStateTxService(jdbcTemplate).evaluate(evaluation));
              });
      evaluationStarted.await(5, TimeUnit.SECONDS);
      allowCommit.countDown();
      retire.get(10, TimeUnit.SECONDS);
      evaluate.get(10, TimeUnit.SECONDS);
    }
    assertEquals(
        0,
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM t_sys_monitor_alert_incident WHERE rule_id=? AND scope_id=? AND"
                + " status IN ('PENDING','FIRING')",
            Integer.class,
            offlineRuleId,
            SCOPE_ID));
  }

  private void await(CountDownLatch latch) {
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(exception);
    }
  }

  private void evaluateAfterBarrier(
      MonitorAlertEvaluation evaluation, CountDownLatch ready, CountDownLatch start) {
    ready.countDown();
    try {
      start.await(5, TimeUnit.SECONDS);
      transactionTemplate.executeWithoutResult(
          status -> new MonitorAlertStateTxService(jdbcTemplate).evaluate(evaluation));
    } catch (DuplicateKeyException ignored) {
      // 另一事务已经赢得 active incident 唯一约束，等价于生产 evaluator 的收敛策略。
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(exception);
    }
  }
}
