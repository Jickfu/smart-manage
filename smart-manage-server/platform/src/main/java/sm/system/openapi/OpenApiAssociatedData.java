package sm.system.openapi;

/** 请求和响应共同约定的 GCM 附加认证数据。 */
public record OpenApiAssociatedData(String version, String algorithm, String keyId,
                                    String direction, String method, String path, String query,
                                    long created, String nonce, String requestId) {
    public byte[] bytes() {
        String canonical = "version=" + version + "\nalgorithm=" + algorithm + "\nkeyId=" + keyId
                + "\ndirection=" + direction + "\nmethod=" + method.toUpperCase() + "\npath=" + path
                + "\nquery=" + query + "\ncreated=" + created + "\nnonce=" + nonce
                + "\nrequestId=" + requestId;
        return canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
