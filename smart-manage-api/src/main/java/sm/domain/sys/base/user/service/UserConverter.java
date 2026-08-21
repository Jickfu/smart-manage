package sm.domain.sys.base.user.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.domain.sys.base.user.model.vo.UserDetailVO;
import sm.domain.sys.base.user.model.vo.UserListVO;
import sm.framework.mapping.SmMapperConfig;

/** 用户模块纯字段转换器，不承担角色和菜单查询。 */
@Mapper(config = SmMapperConfig.class)
interface UserConverter {

    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    UserListVO toListVO(UserEntity entity);

    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    @Mapping(target = "currentOrgId", ignore = true)
    @Mapping(target = "currentOrgName", ignore = true)
    @Mapping(target = "companyName", ignore = true)
    UserInfoVO toInfoVO(UserEntity entity);

    @Mapping(target = "assignments", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    UserDetailVO toDetailVO(UserEntity entity);
}
