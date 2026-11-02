package sm.domain.sys.base.passwordconfig.model.vo;

/** 公开的规则快照，不包含维护身份和版本，不接收候选密码。 */
public record PasswordPolicyVO(int minLength, int maxLength, boolean complexityEnabled) {}
