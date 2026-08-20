package sm.domain.sys.base.org.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.org.model.vo.OrgDetailVO;
import sm.domain.sys.base.org.model.vo.OrgListVO;
import sm.domain.sys.base.org.model.vo.OrgOptionVO;
import sm.domain.sys.base.org.model.vo.OrgTreeVO;

@Mapper(componentModel = "spring")
public interface OrgConverter {
    OrgListVO toListVO(OrgEntity entity);
    @Mapping(target = "parent", ignore = true)
    OrgDetailVO toDetailVO(OrgEntity entity);
    @Mapping(target = "children", ignore = true)
    OrgTreeVO toTreeVO(OrgEntity entity);
    OrgOptionVO toOptionVO(OrgEntity entity);
}
