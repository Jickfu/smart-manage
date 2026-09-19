package sm.system.security.crypto;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HexFormat;

/** 浏览器密码传输使用的 SM2 密钥配置。 */
@Component
@ConfigurationProperties(prefix = "smart-manage.system.security.sm2.js")
public class Sm2Properties implements InitializingBean {

    private static final String PROPERTY_PREFIX = "smart-manage.system.security.sm2.js";
    private static final X9ECParameters CURVE = GMNamedCurves.getByName("sm2p256v1");
    private static final ECDomainParameters DOMAIN = new ECDomainParameters(
            CURVE.getCurve(), CURVE.getG(), CURVE.getN(), CURVE.getH(), CURVE.getSeed());

    private String privateKey;
    private String publicKey;
    private ECPrivateKeyParameters privateKeyParameters;
    private String normalizedPublicKey;

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    ECPrivateKeyParameters privateKeyParameters() {
        return privateKeyParameters;
    }

    String publicKeyHex() {
        return normalizedPublicKey;
    }

    @Override
    public void afterPropertiesSet() {
        byte[] privateKeyBytes = null;
        byte[] publicKeyBytes = null;
        try {
            privateKeyBytes = parsePrivateKey(privateKey);
            publicKeyBytes = parsePublicKey(publicKey);
            BigInteger privateScalar = new BigInteger(1, privateKeyBytes);
            if (privateScalar.signum() <= 0 || privateScalar.compareTo(DOMAIN.getN()) >= 0) {
                throw new IllegalStateException("SM2 私钥必须位于有效标量范围内");
            }

            ECPoint configuredPublicPoint;
            try {
                configuredPublicPoint = DOMAIN.getCurve().decodePoint(publicKeyBytes).normalize();
            } catch (RuntimeException exception) {
                throw new IllegalStateException("SM2 公钥不是有效的 sm2p256v1 曲线点", exception);
            }
            ECPoint derivedPublicPoint = DOMAIN.getG().multiply(privateScalar).normalize();
            if (!derivedPublicPoint.equals(configuredPublicPoint)) {
                throw new IllegalStateException("SM2 公钥与私钥不匹配");
            }

            privateKeyParameters = new ECPrivateKeyParameters(privateScalar, DOMAIN);
            normalizedPublicKey = HexFormat.of().formatHex(configuredPublicPoint.getEncoded(false));
        } finally {
            if (privateKeyBytes != null) {
                java.util.Arrays.fill(privateKeyBytes, (byte) 0);
            }
            if (publicKeyBytes != null) {
                java.util.Arrays.fill(publicKeyBytes, (byte) 0);
            }
            // 完成绑定后不再保留配置文本，缩小私钥在内存中的重复暴露面。
            privateKey = null;
            publicKey = null;
        }
    }

    private byte[] parsePrivateKey(String value) {
        byte[] bytes = parseHex(value, PROPERTY_PREFIX + ".private-key", "SM2 私钥");
        if (bytes.length == 33 && bytes[0] == 0) {
            byte[] normalized = java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
            java.util.Arrays.fill(bytes, (byte) 0);
            return normalized;
        }
        if (bytes.length != 32) {
            java.util.Arrays.fill(bytes, (byte) 0);
            throw new IllegalStateException("SM2 私钥必须是 32 字节，或使用单个 00 前缀的 33 字节正数编码");
        }
        return bytes;
    }

    private byte[] parsePublicKey(String value) {
        byte[] bytes = parseHex(value, PROPERTY_PREFIX + ".public-key", "SM2 公钥");
        if (bytes.length != 65 || bytes[0] != 0x04) {
            java.util.Arrays.fill(bytes, (byte) 0);
            throw new IllegalStateException("SM2 公钥必须是带 04 前缀的 65 字节非压缩曲线点");
        }
        return bytes;
    }

    private byte[] parseHex(String value, String propertyName, String displayName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("必须配置 " + propertyName);
        }
        try {
            return HexFormat.of().parseHex(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(displayName + "必须是有效的十六进制编码", exception);
        }
    }
}
