package sm.domain.sys.base.passwordconfig.service;

import sm.domain.sys.base.passwordconfig.converter.PasswordConfigConverter;
import sm.domain.sys.base.passwordconfig.model.entity.PasswordConfigEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.system.aop.log.BizLog;
import sm.system.security.authorization.AdministratorOnly;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.domain.sys.base.passwordconfig.mapper.PasswordConfigMapper;
import sm.domain.sys.base.passwordconfig.model.form.PasswordConfigSaveForm;
import sm.domain.sys.base.passwordconfig.model.vo.PasswordConfigDetailVO;
import sm.domain.sys.base.passwordconfig.model.vo.PasswordPolicyVO;

/** 密码策略直接读取数据库，多实例不缓存，不在配置异常时降级。 */
@Service
@RequiredArgsConstructor
public class PasswordConfigService {
    private final PasswordConfigMapper mapper;
    private final PasswordConfigTxService txService;
    private final PasswordConfigConverter converter;

    public PasswordPolicyVO policy() {
        var entity = requireCurrent();
        return new PasswordPolicyVO(entity.getMinLength(), 64, entity.getComplexityEnabled());
    }

    @AdministratorOnly
    public PasswordConfigDetailVO singleton() {
        var entity = requireCurrent();
        return converter.toDetailVO(entity);
    }

    @AdministratorOnly
    @BizLog("保存密码策略")
    public Long save(PasswordConfigSaveForm form) {
        return txService.save(form);
    }

    private PasswordConfigEntity requireCurrent() {
        var entity = mapper.current();
        if (entity == null || entity.getMinLength() == null || entity.getMinLength() < 4
                || entity.getMinLength() > 64 || entity.getComplexityEnabled() == null) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "密码策略配置不存在或无效");
        }
        return entity;
    }
}
