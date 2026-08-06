package sm.system.helper;

/**
 * 前端提交的 SM2 密文无法解密。
 */
public class Sm2DecryptionException extends RuntimeException {
	public Sm2DecryptionException(String message, Throwable cause) {
		super(message, cause);
	}
}
