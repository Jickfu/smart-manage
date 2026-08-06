package sm.domain.sys.base.common.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

/**
 * @author Chekfu
 */
public final class CaptchaUtil {
	/** 校验不区分大小写，因此生成空间只保留小写并排除 0/O、1/I/L 等易混淆字符。 */
	private static final String CHARACTERS = "23456789abcdefghjkmnpqrstuvwxyz";
	/** 字符使用固定的高对比深色，禁止随机出接近白色、肉眼不可见的验证码。 */
	private static final Color[] TEXT_COLORS = {
			new Color(17, 94, 163),
			new Color(22, 101, 52),
			new Color(126, 34, 206),
			new Color(153, 27, 27),
			new Color(146, 64, 14)
	};
	private static final SecureRandom RANDOM = new SecureRandom();

	private CaptchaUtil() {
	}

	public static String generateCharCaptcha(int length) {
		StringBuilder captcha = new StringBuilder();
		for (int i = 0; i < length; i++) {
			captcha.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
		}
		return captcha.toString();
	}

	public static BufferedImage generateCaptchaImage(String captcha, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();

		// 设置背景色
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		// 设置字体
		g.setFont(new Font("Arial", Font.BOLD, 30));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 干扰线保持浅色，不能覆盖或掩盖验证码正文。
		for (int i = 0; i < 5; i++) {
			g.setColor(new Color(
					180 + RANDOM.nextInt(60),
					180 + RANDOM.nextInt(60),
					180 + RANDOM.nextInt(60)));
			g.drawLine(RANDOM.nextInt(width), RANDOM.nextInt(height),
					RANDOM.nextInt(width), RANDOM.nextInt(height));
		}

		// 按画布宽度动态分格并居中，避免固定坐标导致最后一个字符被右边界裁切。
		FontMetrics fontMetrics = g.getFontMetrics();
		int horizontalPadding = 8;
		int availableWidth = width - horizontalPadding * 2;
		double cellWidth = (double) availableWidth / captcha.length();
		for (int i = 0; i < captcha.length(); i++) {
			g.setColor(textColorAt(i));
			String character = String.valueOf(captcha.charAt(i));
			int characterWidth = fontMetrics.stringWidth(character);
			int characterX = horizontalPadding
					+ (int) Math.round(i * cellWidth + (cellWidth - characterWidth) / 2);
			g.drawString(character, characterX, 35);
		}

		g.dispose();
		return image;
	}

	static Color textColorAt(int index) {
		return TEXT_COLORS[index % TEXT_COLORS.length];
	}
}
