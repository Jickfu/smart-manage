package sm.domain.sys.base.feature.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.feature.mapper.FeatureDataObjectMapper;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.domain.sys.base.feature.model.vo.FeatureDataObjectVO;
import sm.system.exception.BizException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeatureDataObjectServiceTests {
    private final FeatureMapper featureMapper = mock(FeatureMapper.class);
    private final FeatureDataObjectMapper dataObjectMapper = mock(FeatureDataObjectMapper.class);
    private final FeatureDataObjectService service = new FeatureDataObjectService(featureMapper, dataObjectMapper);

    @Test
    void rejectsMissingFeatureBeforeReadingMetadata() {
        when(featureMapper.selectById(100L)).thenReturn(null);

        assertThrows(BizException.class, () -> service.listByFeatureId(100L));
        verify(dataObjectMapper, never()).selectByFeatureId(100L);
    }

    @Test
    void returnsOrderedMapperProjectionForExistingFeature() {
        FeatureEntity feature = new FeatureEntity();
        feature.setId(100L);
        FeatureDataObjectVO dataObject = new FeatureDataObjectVO();
        dataObject.setObjectName("t_sys_feature");
        when(featureMapper.selectById(100L)).thenReturn(feature);
        when(dataObjectMapper.selectByFeatureId(100L)).thenReturn(List.of(dataObject));

        List<FeatureDataObjectVO> result = service.listByFeatureId(100L);

        assertEquals(List.of(dataObject), result);
    }
}
