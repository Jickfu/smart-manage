package sm.system.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;

/**
 * 服务端敏感配置加密密钥。
 *
 * <p>密钥只允许通过部署配置注入，不能来自数据库、系统参数或管理端页面。</p>
 */
@Component
@ConfigurationProperties(prefix = "smart-manage.system.security.sm4")
public class Sm4Properties implements InitializingBean {

    private static final int KEY_LENGTH = 16;

    private String keyBase64;
    private byte[] keyBytes;

    public void setKeyBase64(String keyBase64) {
        this.keyBase64 = keyBase64;
    }

    /** 返回副本，避免调用方修改配置组件持有的密钥。 */
    public byte[] getKeyBytes() {
        return keyBytes.clone();
    }

    @Override
    public void afterPropertiesSet() {
        if (keyBase64 == null || keyBase64.isBlank()) {
            throw new IllegalStateException("必须配置 smart-manage.system.security.sm4.key-base64");
        }
        try {
            keyBytes = Base64.getDecoder().decode(keyBase64.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("SM4 密钥必须是有效的 Base64 编码", exception);
        }
        if (keyBytes.length != KEY_LENGTH) {
            throw new IllegalStateException("SM4 密钥解码后必须为 16 字节");
        }
        // 完成绑定后不再保留 Base64 文本，缩小密钥在内存中的重复暴露面。
        keyBase64 = null;
    }
}
