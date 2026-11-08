package sm.system.http;

import java.io.ByteArrayOutputStream;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/** 在接收过程中限制字节，不能先完整加载响应再检查大小。 */
final class BoundedHttpBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
    private final long maximumBytes;
    private final long declaredBytes;
    private final CompletableFuture<byte[]> body = new CompletableFuture<>();
    private final ByteArrayOutputStream received = new ByteArrayOutputStream();
    private Flow.Subscription subscription;

    BoundedHttpBodySubscriber(long maximumBytes, long declaredBytes) {
        this.maximumBytes = maximumBytes;
        this.declaredBytes = declaredBytes;
    }

    @Override
    public CompletionStage<byte[]> getBody() {
        return body;
    }

    @Override
    public void onSubscribe(Flow.Subscription supplied) {
        if (subscription != null) {
            supplied.cancel();
            return;
        }
        subscription = supplied;
        if (declaredBytes > maximumBytes) reject();
        else subscription.request(1);
    }

    @Override
    public void onNext(List<ByteBuffer> buffers) {
        if (body.isDone()) return;
        long incomingBytes = 0;
        for (ByteBuffer buffer : buffers) incomingBytes += buffer.remaining();
        if (incomingBytes > maximumBytes - received.size()) {
            reject();
            return;
        }
        // Content-Length 缺失或不可信时仍按实际字节累计；超限批次不进入聚合缓冲。
        for (ByteBuffer buffer : buffers) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            received.writeBytes(bytes);
        }
        subscription.request(1);
    }

    private void reject() {
        // 先完成响应，再取消上游；取消可能同步触发客户端回调，不能让回调等待未完成的正文。
        body.completeExceptionally(new ExternalHttpException(
                ExternalHttpException.FailureType.RESPONSE_TOO_LARGE, "目标服务响应超过大小限制"));
        subscription.cancel();
    }

    @Override
    public void onError(Throwable failure) {
        body.completeExceptionally(failure);
    }

    @Override
    public void onComplete() {
        body.complete(received.toByteArray());
    }
}
