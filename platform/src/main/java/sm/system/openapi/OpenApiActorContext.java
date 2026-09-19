package sm.system.openapi;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 外部 API 请求在当前线程内建立的代理用户上下文。 */
public final class OpenApiActorContext {
    private static final ThreadLocal<Actor> CURRENT = new ThreadLocal<>();

    private OpenApiActorContext() {
    }

    public static Scope open(Long applicationId, String applicationNumber, Long userId,
                             String username, Long orgId, String requestId) {
        if (CURRENT.get() != null) {
            throw new IllegalStateException("外部 API 代理上下文不允许嵌套");
        }
        CURRENT.set(new Actor(applicationId, applicationNumber, userId, username, orgId, requestId));
        return new Scope();
    }

    public static Actor currentOrNull() {
        return CURRENT.get();
    }

    public static Actor requireCurrent() {
        Actor actor = CURRENT.get();
        if (actor == null) {
            throw new BizException(ResultEnum.UNAUTHORIZED, "外部 API 代理身份尚未建立");
        }
        return actor;
    }

    public record Actor(Long applicationId, String applicationNumber, Long userId,
                        String username, Long orgId, String requestId) {
    }

    public static final class Scope implements AutoCloseable {
        private boolean closed;

        private Scope() {
        }

        @Override
        public void close() {
            if (!closed) {
                CURRENT.remove();
                closed = true;
            }
        }
    }
}
