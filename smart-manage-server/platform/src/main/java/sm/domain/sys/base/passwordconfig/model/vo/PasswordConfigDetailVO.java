package sm.domain.sys.base.passwordconfig.model.vo;

public record PasswordConfigDetailVO(Long id, Integer version, int minLength, boolean complexityEnabled) {}
