package sm.domain.sys.monitor.script.service;

record ScriptExecutionConfig(String transactionMode, int timeoutSeconds, int maxOutputLength) {
}
