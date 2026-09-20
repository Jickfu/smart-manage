package sm.system.util;

import java.security.SecureRandom;

/**
 * 随机密码生成工具。
 */
public final class PasswordGeneratorUtil {
    public static final int MIN_PASSWORD_LENGTH = 4;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+";
    private static final String ALL = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordGeneratorUtil() {
    }

    /**
     * 固定从四类字符池各取一个字符，再补齐并安全打乱，确保每个密码都包含四类字符。
     */
    public static String generate(int passwordLength) {
        if (passwordLength < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("密码长度不能小于 " + MIN_PASSWORD_LENGTH);
        }
        char[] password = new char[passwordLength];
        password[0] = randomChar(UPPERCASE);
        password[1] = randomChar(LOWERCASE);
        password[2] = randomChar(DIGITS);
        password[3] = randomChar(SPECIAL);
        for (int index = 4; index < passwordLength; index++) {
            password[index] = randomChar(ALL);
        }
        for (int index = password.length - 1; index > 0; index--) {
            int swapIndex = SECURE_RANDOM.nextInt(index + 1);
            char current = password[index];
            password[index] = password[swapIndex];
            password[swapIndex] = current;
        }
        return new String(password);
    }

    private static char randomChar(String characters) {
        return characters.charAt(SECURE_RANDOM.nextInt(characters.length()));
    }
}
