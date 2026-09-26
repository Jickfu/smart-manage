package sm.domain.sys.base.feature.contract;

import java.util.Collection;
import java.util.List;

/** 按调用方明确声明的功能键读取目录，不推断业务能力或暴露功能管理接口。 */
public interface FeatureDirectoryReader {
    List<FeatureDirectoryReference> findByFeatureKeys(Collection<String> featureKeys);
}
