package sm.domain.sys.monitor.script.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** 超出上限后丢弃后续字节，避免恶意或错误脚本通过输出耗尽堆内存。 */
final class BoundedOutputStream extends OutputStream {
    private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();
    private final int maxBytes;
    private boolean truncated;

    BoundedOutputStream(int maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    public void write(int value) {
        if (delegate.size() < maxBytes) {
            delegate.write(value);
        } else {
            truncated = true;
        }
    }

    @Override
    public void write(byte[] bytes, int offset, int length) {
        int remaining = maxBytes - delegate.size();
        if (remaining > 0) {
            delegate.write(bytes, offset, Math.min(remaining, length));
        }
        if (length > remaining) {
            truncated = true;
        }
    }

    String content() {
        return delegate.toString(StandardCharsets.UTF_8).trim();
    }

    boolean isTruncated() {
        return truncated;
    }
}
