package sm.domain.sys.monitor.script.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 原子模式在执行 JavaScript 前开启事务，脚本调用的默认 REQUIRED 事务将加入此事务。 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class ScriptExecutionTxService {
    private final ScriptExecutor executor;

    ScriptExecutionOutcome execute(ScriptExecutionConfig config, String content) {
        ScriptExecutionOutcome outcome = executor.execute(config, content);
        if (!"SUCCESS".equals(outcome.status())) {
            throw new ScriptExecutionFailure(outcome);
        }
        return outcome;
    }
}
