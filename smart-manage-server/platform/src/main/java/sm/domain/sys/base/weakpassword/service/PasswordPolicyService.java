package sm.domain.sys.base.weakpassword.service;

import sm.domain.sys.base.passwordconfig.service.PasswordConfigService;
import sm.system.util.PasswordGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.util.WeakPasswordUtil;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 所有新密码写入共享的动态策略；不提供公开的匿名密码探测接口。 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {
    public static final int MAX_LENGTH = 64;
    private final WeakPasswordMapper mapper;
    private final PasswordConfigService configService;

    /** 自动生成密码至少20位，始终包含四类字符，再复用完整策略校验。 */
    public int minimumGeneratedLength() {
        return Math.max(20, configService.policy().minLength());
    }

    public String generate(String username) {
        String password = PasswordGeneratorUtil.generate(
                minimumGeneratedLength());
        validate(password, username);
        return password;
    }

    public void validate(String password, String username) {
        if (WeakPasswordUtil.isBlank(password)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码不能为空或全为空白");
        }
        var policy = configService.policy();
        int length = password.codePointCount(0, password.length());
        if (length < policy.minLength() || length > MAX_LENGTH) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码须为" + policy.minLength() + "～64个字符");
        }
        if (password.codePoints().anyMatch(codePoint -> Character.isISOControl(codePoint)
                || codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码不能包含控制字符或无效字符");
        }
        // 特殊字符仅指可打印 ASCII 标点；空格、中文和其他 Unicode 字符不冒充符号。
        if (policy.complexityEnabled() && (!password.matches("(?s).*[A-Z].*")
                || !password.matches("(?s).*[a-z].*") || !password.matches("(?s).*[0-9].*")
                || password.codePoints().noneMatch(codePoint -> codePoint >= 33 && codePoint <= 126
                    && !(codePoint >= 'A' && codePoint <= 'Z')
                    && !(codePoint >= 'a' && codePoint <= 'z')
                    && !(codePoint >= '0' && codePoint <= '9')))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码必须包含大写字母、小写字母、数字和特殊字符");
        }
        // 只拦截账号/产品名称加数字、空格、符号的常见变体，不做子串匹配误伤长口令。
        String letters = contextLetters(password);
        if (!letters.isEmpty() && (letters.equals(contextLetters(username)) || letters.equals("smartmanage"))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码不能使用账号或系统名称的简单变体");
        }
        if (mapper.containsDigest(WeakPasswordUtil.digest(password))) {
            throw new BizException(ResultEnum.PARAM_ERROR, "该密码属于弱口令，请使用其他长口令");
        }
    }

    private static String contextLetters(String value) {
        return value == null ? "" : WeakPasswordUtil.normalize(value).replaceAll("[\\p{N}\\p{P}\\p{S}\\p{Z}\\s]", "");
    }
}
