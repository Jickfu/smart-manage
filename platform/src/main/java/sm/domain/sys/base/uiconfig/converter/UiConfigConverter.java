package sm.domain.sys.base.uiconfig.converter;

import org.mapstruct.Mapper;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigDetailVO;
import sm.infrastructure.mapping.SmMapperConfig;

/** 界面配置纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
public interface UiConfigConverter {
    UiConfigDetailVO toDetailVO(UiConfigEntity entity);
}
