package sm.domain.sys.base.weakpassword.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.weakpassword.mapper.WeakPasswordMapper;
import sm.domain.sys.base.weakpassword.util.WeakPasswordUtil;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/** 所有新密码写入共享的固定策略；不提供公开的匿名密码探测接口。 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {
    public static final int MIN_LENGTH = 15;
    public static final int MAX_LENGTH = 64;
    private final WeakPasswordMapper mapper;

    public void validate(String password, String username) {
        if (WeakPasswordUtil.isBlank(password)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码不能为空或全为空白");
        }
        int length = password.codePointCount(0, password.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码须为15～64个字符");
        }
        if (password.codePoints().anyMatch(codePoint -> Character.isISOControl(codePoint)
                || codePoint >= Character.MIN_SURROGATE && codePoint <= Character.MAX_SURROGATE)) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码不能包含控制字符或无效字符");
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
