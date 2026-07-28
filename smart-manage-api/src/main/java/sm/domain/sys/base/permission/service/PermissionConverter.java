package sm.domain.sys.base.permission.service;

import org.mapstruct.Mapper;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.domain.sys.base.permission.model.vo.PermissionDetailVO;
import sm.domain.sys.base.permission.model.vo.PermissionListVO;
import sm.domain.sys.base.permission.model.vo.PermissionSelectVO;
import sm.framework.mapping.SmMapperConfig;

/** 权限模块纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface PermissionConverter {

    PermissionListVO toListVO(PermissionEntity entity);

    PermissionSelectVO toSelectVO(PermissionEntity entity);

    PermissionDetailVO toDetailVO(PermissionEntity entity);
}
