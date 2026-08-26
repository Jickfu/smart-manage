package sm.system.security.crypto;

/** 浏览器 SM2 密文格式或完整性校验失败。 */
public class Sm2CiphertextException extends RuntimeException {

    public Sm2CiphertextException(String message) {
        super(message);
    }

    public Sm2CiphertextException(String message, Throwable cause) {
        super(message, cause);
    }
}
