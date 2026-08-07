package sm.domain.sys.base.menu.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.vo.MenuDetailVO;
import sm.domain.sys.base.menu.model.vo.MenuListVO;
import sm.domain.sys.base.menu.model.vo.MenuSelectVO;
import sm.domain.sys.base.menu.model.vo.MenuTreeVO;
import sm.framework.mapping.SmMapperConfig;

/** 菜单持久化字段转换器，父菜单与用户菜单树由 Service 组装。 */
@Mapper(config = SmMapperConfig.class)
interface MenuConverter {
    @Mapping(target = "level", expression = "java(entity.getLevel().getCode())")
    @Mapping(target = "appName", ignore = true)
    @Mapping(target = "children", ignore = true)
    MenuTreeVO toTreeVO(MenuEntity entity);

    MenuListVO toListVO(MenuEntity entity);

    MenuSelectVO toSelectVO(MenuEntity entity);

    @Mapping(target = "parent", ignore = true)
    MenuDetailVO toDetailVO(MenuEntity entity);
}
