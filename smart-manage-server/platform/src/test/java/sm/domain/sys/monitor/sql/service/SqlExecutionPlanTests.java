package sm.domain.sys.monitor.sql.service;

import org.junit.jupiter.api.Test;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlExecutionPlanTests {

    @Test
    void acceptsSingleQuery() {
        SqlExecutionPlan plan = SqlExecutionPlan.parse("select 1 as value");
        assertEquals("QUERY", plan.type());
        assertEquals(1, plan.statements().size());
    }

    @Test
    void acceptsMultipleInsertsAsAtomicBatch() {
        SqlExecutionPlan plan = SqlExecutionPlan.parse("insert into a(id) values (1); insert into a(id) values (2)");
        assertEquals("DML", plan.type());
        assertEquals(2, plan.statements().size());
    }

    @Test
    void rejectsMixedMultipleStatements() {
        assertThrows(BizException.class,
                () -> SqlExecutionPlan.parse("insert into a(id) values (1); update a set id = 2"));
    }

    @Test
    void rejectsTransactionControlStatements() {
        assertThrows(BizException.class, () -> SqlExecutionPlan.parse("commit"));
    }
}
