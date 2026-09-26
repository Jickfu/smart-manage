package sm.domain.sys.base.feature.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.feature.contract.FeatureDirectoryReader;
import sm.domain.sys.base.feature.contract.FeatureDirectoryReference;
import sm.domain.sys.base.feature.mapper.FeatureMapper;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureDirectoryService implements FeatureDirectoryReader {
    private final FeatureMapper mapper;

    @Override
    public List<FeatureDirectoryReference> findByFeatureKeys(Collection<String> featureKeys) {
        // 空接入集合不能退化为查询整个功能目录。
        if (featureKeys.isEmpty()) return List.of();
        return mapper.selectDirectoryByFeatureKeys(featureKeys.stream().distinct().toList());
    }
}
