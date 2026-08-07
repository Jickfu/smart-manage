package sm.domain.sys.base.menu.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.app.mapper.AppMapper;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuTreeListForm;
import sm.domain.sys.base.menu.model.vo.MenuTreeVO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuServiceTests {
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final MenuMapper mapper = mock(MenuMapper.class);
    private final AppMapper appMapper = mock(AppMapper.class);
    private final MenuTxService txService = mock(MenuTxService.class);
    private final MenuConverter converter = mock(MenuConverter.class);
    private final MenuService service =
            new MenuService(currentUserContext, mapper, appMapper, txService, converter);

    @Test
    void treeListReturnsMatchedPageWithParentGroupAndAppName() {
        AppEntity app = new AppEntity();
        app.setId(20L);
        app.setName("系统建模");
        when(appMapper.selectList(any())).thenReturn(List.of(app));

        MenuEntity group = menu(100L, 20L, 0L, MenuLevelEnum.CATEGORY, "基础设置", null);
        MenuEntity page = menu(101L, 20L, 100L, MenuLevelEnum.PAGE, "用户", "/sys/base/user");
        when(mapper.selectList(any())).thenReturn(List.of(group, page));
        when(converter.toTreeVO(any())).thenAnswer(invocation -> toTreeVO(invocation.getArgument(0)));

        MenuTreeListForm form = new MenuTreeListForm();
        form.setCloudId(10L);
        form.setKeyword("用户");

        List<MenuTreeVO> result = service.listTree(form);

        assertEquals(1, result.size());
        assertEquals("系统建模", result.getFirst().getAppName());
        assertEquals(1, result.getFirst().getChildren().size());
        assertEquals("用户", result.getFirst().getChildren().getFirst().getName());
    }

    private MenuEntity menu(
            Long id, Long appId, Long parentId, MenuLevelEnum level, String name, String path) {
        MenuEntity entity = new MenuEntity();
        entity.setId(id);
        entity.setAppId(appId);
        entity.setParentId(parentId);
        entity.setLevel(level);
        entity.setName(name);
        entity.setPath(path);
        entity.setSort(1);
        entity.setEnabled(true);
        return entity;
    }

    private MenuTreeVO toTreeVO(MenuEntity entity) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(entity.getId());
        vo.setAppId(entity.getAppId());
        vo.setParentId(entity.getParentId());
        vo.setLevel(entity.getLevel().getCode());
        vo.setName(entity.getName());
        vo.setPath(entity.getPath());
        vo.setSort(entity.getSort());
        vo.setEnabled(entity.getEnabled());
        return vo;
    }
}
