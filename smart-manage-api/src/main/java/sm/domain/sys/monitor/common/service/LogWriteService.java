package sm.domain.sys.monitor.common.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.monitor.loginlog.constant.LoginEventType;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.domain.sys.monitor.loginlog.mapper.LoginLogMapper;
import sm.domain.sys.monitor.operatelog.model.entity.OperateLogEntity;
import sm.domain.sys.monitor.operatelog.mapper.OperateLogMapper;
import sm.system.aop.log.OperateLogPayload;
import sm.system.aop.log.OperateLogWriter;
import sm.system.util.TraceIdUtil;

import java.time.LocalDateTime;

/**
 * 异步日志写入服务（公共能力）
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogWriteService implements OperateLogWriter {
    private final LoginLogMapper loginLogMapper;
    private final OperateLogMapper operateLogMapper;
    @Resource
    @Qualifier("logTaskExecutor")
    private ThreadPoolTaskExecutor logTaskExecutor;

    /**
     * 写入登录/登出日志
     */
    public void writeLogin(LoginLogEntity e) {
        if (e.getCreateTime() == null) {
            e.setCreateTime(LocalDateTime.now());
        }
        if (e.getTraceId() == null) {
            e.setTraceId(TraceIdUtil.getTraceId());
        }
        e.setUsername(truncateColumn(e.getUsername(), 128));
        e.setNickname(truncateColumn(e.getNickname(), 255));
        e.setEventType(truncateColumn(e.getEventType(), 32));
        e.setFailReason(truncateColumn(e.getFailReason(), 512));
        e.setIp(truncateColumn(e.getIp(), 64));
        e.setUserAgent(truncateColumn(e.getUserAgent(), 1024));
        e.setTraceId(truncateColumn(e.getTraceId(), 64));
        e.setGrantId(truncateColumn(e.getGrantId(), 64));
        e.setGrantReason(truncateColumn(e.getGrantReason(), 500));
        runAsync(() -> loginLogMapper.insert(e));
    }

    /**
     * 写入操作日志（内部接口，BizLogAspect 通过 {@link #write(OperateLogPayload)} 调用）
     */
    public void writeOper(OperateLogEntity entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setBizName(truncateColumn(entity.getBizName(), 256));
        entity.setRequestMethod(truncateColumn(entity.getRequestMethod(), 32));
        entity.setRequestUri(truncateColumn(entity.getRequestUri(), 512));
        entity.setIp(truncateColumn(entity.getIp(), 64));
        entity.setUserAgent(truncateColumn(entity.getUserAgent(), 1024));
        entity.setClassName(truncateColumn(entity.getClassName(), 256));
        entity.setMethodName(truncateColumn(entity.getMethodName(), 128));
        entity.setUsername(truncateColumn(entity.getUsername(), 128));
        entity.setTraceId(truncateColumn(entity.getTraceId(), 64));
        runAsync(() -> operateLogMapper.insert(entity));
    }

    /**
     * 实现 OperateLogWriter 接口 — 将 Payload 转换为实体后写入
     */
    @Override
    public void write(OperateLogPayload payload) {
        OperateLogEntity entity = new OperateLogEntity();
        entity.setBizName(payload.bizName());
        entity.setSuccess(payload.success());
        entity.setErrorMsg(payload.errorMsg());
        entity.setRequestMethod(payload.requestMethod());
        entity.setRequestUri(payload.requestUri());
        entity.setIp(payload.ip());
        entity.setUserAgent(payload.userAgent());
        entity.setClassName(payload.className());
        entity.setMethodName(payload.methodName());
        entity.setDurationMs(payload.durationMs());
        entity.setRequestParams(payload.requestParams());
        entity.setResponseBody(payload.responseBody());
        entity.setUserId(payload.userId());
        entity.setUsername(payload.username());
        entity.setTraceId(payload.traceId());
        writeOper(entity);
    }

    /**
     * 登录失败（在 Web 请求线程中采集上下文后入队）
     */
    public void writeLoginFailed(String username, String failReason, String ip, String userAgent) {
        LoginLogEntity entity = new LoginLogEntity();
        entity.setUsername(username);
        entity.setEventType(LoginEventType.LOGIN_FAILURE.name());
        entity.setSuccess(false);
        entity.setFailReason(failReason);
        entity.setIp(ip);
        entity.setUserAgent(StringUtils.hasText(userAgent) ? userAgent : null);
        entity.setCreateTime(LocalDateTime.now());
        writeLogin(entity);
    }

    /** 在请求线程完成身份快照后写入非正式会话认证事件。 */
    public void writeAuthenticationEvent(Long userId, String username, String nickname,
                                         LoginEventType eventType, boolean success,
                                         String reason, String ip, String userAgent) {
        LoginLogEntity entity = new LoginLogEntity();
        entity.setUserId(userId);
        entity.setUsername(username);
        entity.setNickname(nickname);
        entity.setEventType(eventType.name());
        entity.setSuccess(success);
        entity.setFailReason(reason);
        entity.setIp(ip);
        entity.setUserAgent(StringUtils.hasText(userAgent) ? userAgent : null);
        entity.setCreateTime(LocalDateTime.now());
        writeLogin(entity);
    }

    /** 记录一次性代登录凭证的生成或成功消费，不包含明文凭证及其摘要。 */
    public void writeTemporaryLoginEvent(Long targetUserId, String targetUsername, String targetName,
                                         Long issuerUserId, String grantId, String reason,
                                         LocalDateTime expiresAt, LoginEventType eventType,
                                         String ip, String userAgent) {
        LoginLogEntity entity = new LoginLogEntity();
        entity.setUserId(targetUserId);
        entity.setUsername(targetUsername);
        entity.setNickname(targetName);
        entity.setIssuerUserId(issuerUserId);
        entity.setGrantId(grantId);
        entity.setGrantReason(reason);
        entity.setGrantExpiresAt(expiresAt);
        entity.setEventType(eventType.name());
        entity.setSuccess(true);
        entity.setIp(ip);
        entity.setUserAgent(StringUtils.hasText(userAgent) ? userAgent : null);
        entity.setCreateTime(LocalDateTime.now());
        writeLogin(entity);
    }

    private void runAsync(Runnable r) {
        if (logTaskExecutor == null) {
            r.run();
            return;
        }
        logTaskExecutor.execute(() -> {
            try {
                r.run();
            } catch (Exception e) {
                log.warn("异步日志写入失败", e);
            }
        });
    }

    /** 严格适配 varchar 长度，截断结果本身不能再次超过数据库字段上限。 */
    private String truncateColumn(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
