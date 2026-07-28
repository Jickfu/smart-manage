package sm.domain.sys.base.basicdata.service;

import org.mapstruct.Mapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntryEntity;
import sm.domain.sys.base.basicdata.model.vo.BasicDataEntryVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataListVO;
import sm.framework.mapping.SmMapperConfig;

/** 基础数据纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface BasicDataConverter {
    BasicDataListVO toListVO(BasicDataEntity entity);
    BasicDataEntryVO toEntryVO(BasicDataEntryEntity entity);
}
