package sm.domain.sys.base.uiconfig.service;

import org.mapstruct.Mapper;
import sm.domain.sys.base.uiconfig.model.entity.UiConfigEntity;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigDetailVO;
import sm.domain.sys.base.uiconfig.model.vo.UiConfigListVO;
import sm.framework.mapping.SmMapperConfig;

/** 界面配置纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface UiConfigConverter {
    UiConfigListVO toListVO(UiConfigEntity entity);
    UiConfigDetailVO toDetailVO(UiConfigEntity entity);
}
