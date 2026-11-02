package sm.domain.sys.base.passwordconfig.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.passwordconfig.mapper.PasswordConfigMapper;
import sm.domain.sys.base.passwordconfig.model.form.PasswordConfigSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class PasswordConfigTxService {
    private final PasswordConfigMapper mapper;

    public Long save(PasswordConfigSaveForm form) {
        if (!Objects.equals(form.getId(), 1L) || form.getVersion() == null
                || form.getMinLength() == null || form.getMinLength() < 4 || form.getMinLength() > 64
                || form.getComplexityEnabled() == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "密码最小长度须为4～64的整数，且必须指定复杂度开关");
        }
        var entity = mapper.current();
        if (entity == null) throw new BizException(ResultEnum.CONFIG_ERROR, "密码策略配置不存在");
        if (!Objects.equals(entity.getVersion(), form.getVersion())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "密码策略已被修改，请刷新后重试");
        }
        entity.setMinLength(form.getMinLength());
        entity.setComplexityEnabled(form.getComplexityEnabled());
        if (mapper.updateById(entity) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "密码策略已被修改，请刷新后重试");
        }
        return entity.getId();
    }
}
