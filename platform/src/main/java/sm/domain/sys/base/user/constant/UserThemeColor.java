package sm.domain.sys.base.user.constant;

import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Set;

/**
 * 用户可选的第 6 阶主题色。
 */
public final class UserThemeColor {
    public static final String DEFAULT = "#276FF5";

    private static final Set<String> SUPPORTED_COLORS = Set.of(
            "#F90D58",
            "#FB2323",
            "#FF5F1F",
            "#FF991C",
            "#16B8B1",
            "#1BA854",
            "#77C404",
            "#FDC200",
            "#16B0F1",
            "#276FF5",
            "#0E5FD8",
            "#701DF0"
    );

    private UserThemeColor() {
    }

    public static String normalizeRequired(String themeColor) {
        if (themeColor == null || themeColor.isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "主题色不能为空");
        }
        String normalized = themeColor.trim().toUpperCase();
        if (!SUPPORTED_COLORS.contains(normalized)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "主题色不在可选范围内");
        }
        return normalized;
    }
}
