package sm.domain.sys.base.sysparam.service;

import org.mapstruct.Mapper;
import sm.domain.sys.base.sysparam.model.entity.SysParamEntity;
import sm.domain.sys.base.sysparam.model.vo.SysParamVO;
import sm.framework.mapping.SmMapperConfig;

/** 系统参数纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface SysParamConverter {
    SysParamVO toVO(SysParamEntity entity);
}
