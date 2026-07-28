package sm.domain.sys.monitor.common.service;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.system.aop.log.LogPayloadUtil;
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
        runAsync(() -> loginLogMapper.insert(e));
    }

    /**
     * 写入操作日志（内部接口，BizLogAspect 通过 {@link #write(OperateLogPayload)} 调用）
     */
    public void writeOper(OperateLogEntity entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
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
        entity.setEventType("LOGIN");
        entity.setSuccess(false);
        entity.setFailReason(truncateMsg(failReason));
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

    private String truncateMsg(String s) {
        return s == null ? null : LogPayloadUtil.truncate(s, 500);
    }
}
