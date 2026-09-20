package sm.domain.sys.monitor.script.service;

record ScriptExecutionOutcome(String status, String output, String errorMessage, int executeDuration,
                              boolean truncated) {
}
