package sm.system.security.crypto;

/** 浏览器密码密文的系统安全边界。 */
public interface BrowserPasswordCipher {

    /** 解密前端 sm2.js 按 C1C3C2 模式产生的密文。 */
    String decrypt(String ciphertext);

    /** 返回浏览器加密所需的公开密钥，不包含任何服务端秘密。 */
    String publicKey();
}
