package sm.domain.sys.monitor.script.service;

final class ScriptExecutionFailure extends RuntimeException {
    private final ScriptExecutionOutcome outcome;

    ScriptExecutionFailure(ScriptExecutionOutcome outcome) {
        super(outcome.errorMessage());
        this.outcome = outcome;
    }

    ScriptExecutionOutcome getOutcome() {
        return outcome;
    }
}
