package sm.domain.sys.base.feature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.domain.sys.base.feature.model.form.FeatureSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class FeatureTxService {
    private final FeatureMapper mapper;

    void save(FeatureSaveForm form) {
        FeatureEntity entity = mapper.selectById(form.getId());
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "功能不存在");
        if (!Objects.equals(entity.getVersion(), form.getVersion())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "功能已被其他用户修改，请刷新后重试");
        }
        entity.setCustomName(normalize(form.getCustomName()));
        entity.setCustomSeq(form.getCustomSeq());
        entity.setDescription(normalize(form.getDescription()));
        entity.setVisible(form.getVisible());
        if (mapper.updateById(entity) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "功能已被其他用户修改，请刷新后重试");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
