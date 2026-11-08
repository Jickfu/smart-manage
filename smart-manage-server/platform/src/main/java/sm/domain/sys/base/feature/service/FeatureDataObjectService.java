package sm.domain.sys.base.feature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.feature.mapper.FeatureDataObjectMapper;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.vo.FeatureDataObjectVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.authorization.AdministratorOnly;

import java.util.List;

/** 数据库对象属于管理员排障元数据，不进入普通功能详情响应。 */
@Service
@AdministratorOnly
@RequiredArgsConstructor
public class FeatureDataObjectService {
    private final FeatureMapper featureMapper;
    private final FeatureDataObjectMapper dataObjectMapper;

    public List<FeatureDataObjectVO> listByFeatureId(Long featureId) {
        if (featureMapper.selectById(featureId) == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "功能不存在");
        }
        return dataObjectMapper.selectByFeatureId(featureId);
    }
}
