package sm.domain.sys.monitor.arthas.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.monitor.arthas.model.vo.ArthasResultVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.aop.log.BizLog;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import sm.system.util.TraceIdUtil;
import java.util.regex.Pattern;

/**
 * Arthas 命令代理服务：通过 Telnet 协议连接本地 Arthas 执行命令
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArthasService {
	private final CurrentUserContext currentUserContext;
	@Value("${smart-manage.instance-id}")
	private String instanceId;

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 3658;
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int MAX_SESSIONS = 4;
    private static final int MAX_SESSION_OUTPUT_CHARS = 256 * 1024;
    private static final long SESSION_IDLE_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(5);
    private static final Pattern ANSI_PATTERN = Pattern.compile("\\[[;\\d]*m");

    // 一次性命令
    private static final Set<String> ONE_SHOT_COMMANDS = Set.of(
            "thread", "jvm", "memory", "dashboard", "vmoption", "logger",
            "classloader", "mbean", "getstatic", "ognl", "sc", "sm", "dump",
            "heapdump", "vmtool", "jad", "mc", "retransform", "redefine"
    );

    // 持续命令
    private static final Set<String> CONTINUOUS_COMMANDS = Set.of(
            "trace", "watch", "stack", "tt", "monitor"
    );

    // 活跃的持续命令会话
    private final ConcurrentHashMap<String, ArthasSession> sessions = new ConcurrentHashMap<>();
    private final Semaphore sessionPermits = new Semaphore(MAX_SESSIONS);

    // 线程池用于异步读取持续命令输出
    private final ExecutorService executor = Executors.newFixedThreadPool(MAX_SESSIONS, r -> {
        Thread t = new Thread(r, "arthas-session-");
        t.setDaemon(true);
        return t;
    });

    /**
     * 执行一次性命令
     */
    @BizLog(value = "执行Arthas命令", recordRequest = false, recordResponse = false)
    public ArthasResultVO execute(String command, String args) {
        currentUserContext.checkAdministrator();
        if (!ONE_SHOT_COMMANDS.contains(command)) {
            if (CONTINUOUS_COMMANDS.contains(command)) {
                throw new BizException(ResultEnum.PARAM_ERROR, "命令 '" + command + "' 是持续命令，请使用 /start 端点");
            }
            throw new BizException(ResultEnum.PARAM_ERROR, "不支持的命令: " + command);
        }
        String fullCmd = args != null && !args.isEmpty() ? command + " " + args : command;
        String output = execAndRead(fullCmd);
        return ArthasResultVO.ok(output).withInstanceId(instanceId);
    }

    /**
     * 启动持续命令（trace/watch/stack/tt/monitor）
     */
    @BizLog(value = "启动Arthas会话", recordRequest = false, recordResponse = false)
    public ArthasResultVO start(String command, String args) {
        currentUserContext.checkAdministrator();
        if (!CONTINUOUS_COMMANDS.contains(command)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "命令 '" + command + "' 不是持续命令，请使用 /execute 端点");
        }
        String fullCmd = args != null && !args.isEmpty() ? command + " " + args : command;
        if (!sessionPermits.tryAcquire()) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "Arthas 活跃会话已达到上限，请先结束已有会话");
        }
        String sessionId = UUID.randomUUID().toString().substring(0, 8);

        ArthasSession session = new ArthasSession(sessionId, fullCmd);
        sessions.put(sessionId, session);
        try {
            executor.submit(TraceIdUtil.wrap(() -> runSession(session)));
        } catch (RuntimeException exception) {
            sessions.remove(sessionId, session);
            sessionPermits.release();
            throw exception;
        }
        return ArthasResultVO.running(sessionId, "会话已启动，命令: " + fullCmd).withInstanceId(instanceId);
    }

    /**
     * 停止持续命令
     */
    @BizLog("停止Arthas会话")
    public ArthasResultVO stop(String requestedInstanceId, String sessionId) {
        currentUserContext.checkAdministrator();
        requireCurrentInstance(requestedInstanceId);
        ArthasSession session = sessions.remove(sessionId);
        if (session == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "会话不存在或已结束: " + sessionId);
        }
        session.stop();
        return ArthasResultVO.ok(session.getOutput()).withInstanceId(instanceId);
    }

    /**
     * 读取持续命令输出
     */
    public ArthasResultVO read(String requestedInstanceId, String sessionId) {
        currentUserContext.checkAdministrator();
        requireCurrentInstance(requestedInstanceId);
        ArthasSession session = sessions.get(sessionId);
        if (session == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "会话不存在或已结束: " + sessionId);
        }
        session.touch();
        String output = session.getOutput();
        boolean running = !session.isStopped();
        return (running ? ArthasResultVO.running(sessionId, output) : ArthasResultVO.ok(output))
                .withInstanceId(instanceId);
    }

    private void requireCurrentInstance(String requestedInstanceId) {
        if (!instanceId.equals(requestedInstanceId)) {
            throw new BizException(ResultEnum.PARAM_ERROR,
                    "Arthas 会话属于实例 " + requestedInstanceId + "，当前实例为 " + instanceId);
        }
    }

    // ── 内部实现 ──

    private String execAndRead(String command) {
        TelnetClient client = new TelnetClient();
        client.setConnectTimeout(CONNECT_TIMEOUT);
        try {
            client.connect(HOST, PORT);
            client.setSoTimeout(READ_TIMEOUT_MS);

            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();

            // 读取欢迎横幅直到出现提示符 "$"
            readUntilPrompt(in);

            // 发送命令
            out.write((command + "\n").getBytes());
            out.flush();

            // 读取命令输出
            String result = readUntilPrompt(in);
            // 去除回显的命令行和末尾提示符
            result = stripCommandEchoAndPrompt(result, command);
            return stripAnsi(result).trim();
        } catch (Exception e) {
            log.error("Arthas telnet 执行失败: {}", command, e);
            throw new BizException(ResultEnum.EXTERNAL_SERVICE_ERROR, "Arthas 命令执行失败");
        } finally {
            try {
                client.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    private void runSession(ArthasSession session) {
        TelnetClient client = new TelnetClient();
        client.setConnectTimeout(CONNECT_TIMEOUT);
        try {
            client.connect(HOST, PORT);
            client.setSoTimeout(5000);
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();

            readUntilPrompt(in);
            out.write((session.command + "\n").getBytes());
            out.flush();

            // 持续读取直到停止
            byte[] buf = new byte[4096];
            while (!session.isStopped() && !session.isIdleExpired()) {
                int available = in.available();
                if (available > 0) {
                    int len = in.read(buf, 0, Math.min(available, buf.length));
                    if (len > 0) {
                        String chunk = new String(buf, 0, len);
                        session.appendOutput(stripAnsi(chunk));
                    }
                } else {
                    Thread.sleep(200);
                }
            }
            // 发送 Ctrl+C 停止 Arthas 命令
            out.write(new byte[]{0x03});
            out.flush();
            Thread.sleep(500);
            // 读取剩余输出
            int avail = in.available();
            if (avail > 0) {
                byte[] remaining = new byte[avail];
                int len = in.read(remaining);
                if (len > 0) {
                    session.appendOutput(stripAnsi(new String(remaining, 0, len)));
                }
            }
        } catch (Exception e) {
            if (!session.isStopped()) {
                session.appendOutput("\n[连接异常] " + e.getMessage());
            }
        } finally {
            session.markStopped();
            sessions.remove(session.id, session);
            sessionPermits.release();
            try {
                client.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    private String readUntilPrompt(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < READ_TIMEOUT_MS) {
            int available = in.available();
            if (available > 0) {
                int len = in.read(buf, 0, Math.min(available, buf.length));
                bos.write(buf, 0, len);
                String current = bos.toString();
                // Arthas 提示符以 "$" 结尾
                if (current.endsWith("$ ")) {
                    break;
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        return bos.toString();
    }

    private String stripCommandEchoAndPrompt(String output, String command) {
        // 去除 telnet 回显的命令行和末尾提示符
        String result = output;
        if (result.startsWith(command)) {
            result = result.substring(command.length());
        }
        if (result.endsWith("$ ")) {
            result = result.substring(0, result.length() - 2);
        }
        return result.trim();
    }

    static String stripAnsi(String text) {
        if (text == null) return "";
        return ANSI_PATTERN.matcher(text).replaceAll("");
    }

    @PreDestroy
    void shutdown() {
        for (ArthasSession session : sessions.values()) {
            session.stop();
        }
        executor.shutdownNow();
    }

    // ── 会话内部类 ──

    private static class ArthasSession {
        final String id;
        final String command;
        final StringBuilder output = new StringBuilder();
        volatile boolean stopped = false;
        volatile long lastAccessTime = System.currentTimeMillis();

        ArthasSession(String id, String command) {
            this.id = id;
            this.command = command;
        }

        synchronized void appendOutput(String text) {
            int remaining = MAX_SESSION_OUTPUT_CHARS - output.length();
            if (remaining <= 0) {
                stopped = true;
                return;
            }
            output.append(text, 0, Math.min(text.length(), remaining));
            if (text.length() > remaining) {
                stopped = true;
            }
        }

        synchronized String getOutput() {
            return output.toString();
        }

        void stop() {
            stopped = true;
        }

        boolean isStopped() {
            return stopped;
        }

        void touch() {
            lastAccessTime = System.currentTimeMillis();
        }

        boolean isIdleExpired() {
            return System.currentTimeMillis() - lastAccessTime >= SESSION_IDLE_TIMEOUT_MS;
        }

        void markStopped() {
            stopped = true;
        }
    }
}
