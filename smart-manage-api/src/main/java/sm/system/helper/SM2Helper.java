package sm.system.helper;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.SM2;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sm.system.util.StringUtil;

/**
 * @author Chekfu
 */
@Component
public class SM2Helper {
	@Value("${smart-manage.sm2.js.private-key}")
	private String privateKey;

	@Value("${smart-manage.sm2.js.public-key}")
	private String publicKey;

	private static String staticPrivateKey;
	private static String staticPublicKey;

	@PostConstruct
	public void init() {
		staticPrivateKey = this.privateKey;
		staticPublicKey = this.publicKey;
	}

	/**
	 * 加密
	 */
	public static String encrypt(String data) {
		try {
			if (checkKeyIsEmpty(staticPrivateKey, staticPublicKey)) {
				SM2 sm2 = new SM2(staticPrivateKey, staticPublicKey);
				sm2.setMode(SM2Engine.Mode.C1C3C2);
				return sm2.encryptHex(data, KeyType.PublicKey);
			}
			return data;
		} catch (Exception e) {
			throw new RuntimeException("sm2加密失败" + e);
		}
	}

	/** 浏览器端加密所需的公开密钥，不包含任何服务端秘密。 */
	public static String getPublicKey() {
		return staticPublicKey;
	}

	/**
	 * 解密
	 */
	public static String decrypt(String data) {
		try {
			if (checkKeyIsEmpty(staticPrivateKey, staticPublicKey)) {
				SM2 sm2 = new SM2(staticPrivateKey, staticPublicKey);
				sm2.setMode(SM2Engine.Mode.C1C3C2);
				return sm2.decryptStr(data, KeyType.PrivateKey);
			}
			return data;
		} catch (Exception e) {
			throw new Sm2DecryptionException("SM2 解密失败", e);
		}
	}

	/**
	 * 解密浏览器端 sm2.js 产生的密文。
	 *
	 * <p>当前前端库省略非压缩椭圆曲线点的 {@code 04} 前缀。Hutool 仅在首字节不是
	 * {@code 02}/{@code 03}/{@code 04} 时自动补齐，因此当 C1 的 X 坐标恰好以这些
	 * 字节开头时会被误判为已带前缀并随机解密失败。此入口依据前端协议统一补齐前缀。</p>
	 */
	public static String decryptJsCiphertext(String data) {
		if (data == null || data.length() < 192 || (data.length() & 1) != 0 || !data.matches("[0-9a-fA-F]+")) {
			throw new Sm2DecryptionException("SM2 密文格式无效", null);
		}
		return decrypt("04" + data);
	}

	private static boolean checkKeyIsEmpty(String privateKey, String publicKey) {
		return !StringUtil.isEmpty(privateKey) && !StringUtil.isEmpty(publicKey);
	}
}
