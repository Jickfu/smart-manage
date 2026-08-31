package sm.domain.sys.base.feature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

/** 功能目录模块向同领域协作者提供的只读引用查询。 */
@Service
@RequiredArgsConstructor
public class FeatureReferenceService {
    private final FeatureMapper mapper;

    public FeatureEntity require(Long id) {
        FeatureEntity entity = mapper.selectById(id);
        if (entity == null) throw new BizException(ResultEnum.NOT_FOUND, "功能不存在");
        return entity;
    }

    public List<FeatureEntity> findAll() {
        return mapper.selectList(null);
    }
}
