package sm.domain.sys.base.basicdata.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.basicdata.model.entity.BasicDataCategoryEntity;
import sm.domain.sys.base.basicdata.model.entity.BasicDataItemEntity;
import sm.domain.sys.base.basicdata.model.vo.BasicDataCategoryVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataItemDetailVO;
import sm.domain.sys.base.basicdata.model.vo.BasicDataListVO;
import sm.infrastructure.mapping.SmMapperConfig;

@Mapper(config = SmMapperConfig.class)
interface BasicDataConverter {
    @Mapping(target = "domainName", ignore = true)
    BasicDataCategoryVO toCategoryVO(BasicDataCategoryEntity entity);
    @Mapping(target = "categoryName", ignore = true)
    BasicDataListVO toListVO(BasicDataItemEntity entity);
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "parent", ignore = true)
    BasicDataItemDetailVO toDetailVO(BasicDataItemEntity entity);
}
