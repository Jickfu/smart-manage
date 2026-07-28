package sm.domain.sys.base.cloud.service;

import org.mapstruct.Mapper;
import sm.domain.sys.base.cloud.model.entity.CloudEntity;
import sm.domain.sys.base.cloud.model.vo.CloudDetailVO;
import sm.domain.sys.base.cloud.model.vo.CloudListVO;
import sm.domain.sys.base.cloud.model.vo.CloudSelectVO;
import sm.framework.mapping.SmMapperConfig;

/** 云模块纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface CloudConverter {

    CloudListVO toListVO(CloudEntity entity);

    CloudSelectVO toSelectVO(CloudEntity entity);

    CloudDetailVO toDetailVO(CloudEntity entity);
}
