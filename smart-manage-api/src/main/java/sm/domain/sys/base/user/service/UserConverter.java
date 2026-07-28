package sm.domain.sys.base.user.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.domain.sys.base.user.model.vo.UserListVO;
import sm.framework.mapping.SmMapperConfig;

/** 用户模块纯字段转换器，不承担角色和菜单查询。 */
@Mapper(config = SmMapperConfig.class)
interface UserConverter {

    UserListVO toListVO(UserEntity entity);

    @Mapping(target = "roleIds", ignore = true)
    @Mapping(target = "menus", ignore = true)
    UserInfoVO toInfoVO(UserEntity entity);
}
