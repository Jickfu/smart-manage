package sm.domain.sys.base.basicdata.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataEntryEntity;
import sm.domain.sys.base.basicdata.model.vo.BasicDataEntryVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataDetailVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataListVO;
import sm.framework.mapping.SmMapperConfig;

/** 基础数据纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface BasicDataConverter {
    BasicDataListVO toListVO(BasicDataEntity entity);
    @Mapping(target = "entrys", ignore = true)
    BasicDataDetailVO toDetailVO(BasicDataEntity entity);
    BasicDataEntryVO toEntryVO(BasicDataEntryEntity entity);
}
