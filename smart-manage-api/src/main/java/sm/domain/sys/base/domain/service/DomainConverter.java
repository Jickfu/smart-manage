package sm.domain.sys.base.domain.service;

import org.mapstruct.Mapper;
import sm.domain.sys.base.domain.model.entity.DomainEntity;
import sm.domain.sys.base.domain.model.vo.DomainDetailVO;
import sm.domain.sys.base.domain.model.vo.DomainListVO;
import sm.domain.sys.base.domain.model.vo.DomainSelectVO;
import sm.framework.mapping.SmMapperConfig;

/** 领域模块纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface DomainConverter {

    DomainListVO toListVO(DomainEntity entity);

    DomainSelectVO toSelectVO(DomainEntity entity);

    DomainDetailVO toDetailVO(DomainEntity entity);
}
