package sm.domain.sys.base.common.util;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptchaUtilTests {
	@Test
	void everyCaptchaCharacterIsRenderedInsideItsCell() {
		BufferedImage image = CaptchaUtil.generateCaptchaImage("abcde", 160, 50);

		for (int characterIndex = 0; characterIndex < 5; characterIndex++) {
			int startX = characterIndex * image.getWidth() / 5;
			int endX = (characterIndex + 1) * image.getWidth() / 5;
			assertTrue(hasDarkTextPixel(image, startX, endX),
					"验证码第 " + (characterIndex + 1) + " 个字符未完整绘制");
		}
	}

	@Test
	void everyCaptchaTextColorHasReadableContrastAgainstWhite() {
		for (int index = 0; index < 5; index++) {
			Color color = CaptchaUtil.textColorAt(index);

			assertTrue(contrastAgainstWhite(color) >= 4.5,
					() -> "验证码字符颜色对比度不足: " + color);
		}
	}

	private double contrastAgainstWhite(Color color) {
		double relativeLuminance = 0.2126 * linear(color.getRed())
				+ 0.7152 * linear(color.getGreen())
				+ 0.0722 * linear(color.getBlue());
		return 1.05 / (relativeLuminance + 0.05);
	}

	private double linear(int channel) {
		double value = channel / 255.0;
		return value <= 0.04045 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
	}

	private boolean hasDarkTextPixel(BufferedImage image, int startX, int endX) {
		for (int x = startX; x < endX; x++) {
			for (int y = 5; y < 42; y++) {
				Color pixel = new Color(image.getRGB(x, y));
				double perceivedBrightness = 0.2126 * pixel.getRed()
						+ 0.7152 * pixel.getGreen()
						+ 0.0722 * pixel.getBlue();
				if (perceivedBrightness < 170) {
					return true;
				}
			}
		}
		return false;
	}
}
