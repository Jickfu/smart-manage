package sm.domain.sys.base.org.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.org.model.vo.OrgListVO;
import sm.framework.mapping.SmMapperConfig;

/** 组织模块纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface OrgConverter {

    @Mapping(target = "remark", ignore = true)
    OrgListVO toListVO(OrgEntity entity);
}
