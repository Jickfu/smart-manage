package sm.system.openapi;

/** AES/SM4 GCM 传输信封，认证标签与密文分开编码以便异构客户端接入。 */
public record OpenApiEncryptedPayload(String version, String algorithm, String keyId,
                                      String iv, String ciphertext, String tag) {
}
