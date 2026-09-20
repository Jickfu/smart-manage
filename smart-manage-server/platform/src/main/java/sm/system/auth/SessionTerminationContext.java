package sm.system.auth;

/** 将同步会话终止命令的原因传递给 Sa-Token 监听器，不保存令牌。 */
public final class SessionTerminationContext {
    private static final ThreadLocal<SessionTerminationReason> REASON = new ThreadLocal<>();

    private SessionTerminationContext() { }

    public static SessionTerminationReason current() {
        return REASON.get();
    }

    public static void run(SessionTerminationReason reason, Runnable action) {
        SessionTerminationReason previous = REASON.get();
        try {
            REASON.set(reason);
            action.run();
        } finally {
            if (previous == null) REASON.remove();
            else REASON.set(previous);
        }
    }
}
