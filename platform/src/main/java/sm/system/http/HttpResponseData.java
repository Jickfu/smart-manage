package sm.system.http;

import java.util.List;
import java.util.Map;

/** 出站 HTTP 响应，保留状态码和响应头供调用方按第三方协议判断。 */
public record HttpResponseData<T>(int statusCode, Map<String, List<String>> headers, T body) {
    public HttpResponseData {
        headers = Map.copyOf(headers);
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }
}
