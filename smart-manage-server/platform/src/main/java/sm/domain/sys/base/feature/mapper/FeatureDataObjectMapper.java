package sm.domain.sys.base.feature.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.feature.model.vo.FeatureDataObjectVO;

import java.util.List;

@Mapper
public interface FeatureDataObjectMapper {
    List<FeatureDataObjectVO> selectByFeatureId(@Param("featureId") Long featureId);
}
