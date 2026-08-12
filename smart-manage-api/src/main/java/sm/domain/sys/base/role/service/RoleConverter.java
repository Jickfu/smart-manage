package sm.domain.sys.base.role.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.domain.sys.base.role.model.vo.RoleDetailVO;
import sm.domain.sys.base.role.model.vo.RoleListVO;
import sm.domain.sys.base.role.model.vo.RoleSelectVO;
import sm.framework.mapping.SmMapperConfig;

/** 角色纯字段转换器，权限关系由 RoleService 显式组装。 */
@Mapper(config = SmMapperConfig.class)
interface RoleConverter {

    RoleSelectVO toSelectVO(RoleEntity entity);

    RoleListVO toListVO(RoleEntity entity);

    @Mapping(target = "permissionIds", ignore = true)
    RoleDetailVO toDetailVO(RoleEntity entity);
}
