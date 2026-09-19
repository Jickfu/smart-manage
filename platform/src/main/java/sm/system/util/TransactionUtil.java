package sm.system.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务生命周期工具。
 *
 * @author Chekfu
 */
public final class TransactionUtil {

    private TransactionUtil() {
    }

    /**
     * 在当前数据库事务成功提交后执行外部副作用；无事务时立即执行。
     */
    public static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
