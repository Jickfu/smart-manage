package sm.domain.sys.base.weakpassword.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;

/** 弱口令按完整词条匹配；规范化仅用于黑名单，不改变用户真实密码。 */
public final class WeakPasswordUtil {
    private WeakPasswordUtil() { }

    public static String normalize(String word) {
        return Normalizer.normalize(word, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
    }

    public static boolean isBlank(String word) {
        return word == null || word.codePoints().allMatch(codePoint -> Character.isWhitespace(codePoint)
                || Character.isSpaceChar(codePoint) || codePoint == 0xFEFF);
    }

    /** 查询只传摘要，避免候选密码进入 SQL 参数诊断。 */
    public static String digest(String word) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(normalize(word).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境缺少 SHA-256", exception);
        }
    }
}
