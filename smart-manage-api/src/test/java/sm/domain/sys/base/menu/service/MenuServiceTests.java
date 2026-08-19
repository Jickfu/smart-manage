package sm.domain.sys.base.menu.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.app.mapper.AppMapper;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuTreeListForm;
import sm.domain.sys.base.menu.model.vo.MenuAppInfoVO;
import sm.domain.sys.base.menu.model.vo.MenuTreeVO;
import sm.domain.sys.base.menu.model.vo.MenuDetailVO;
import sm.domain.sys.base.menu.model.vo.MenuVO;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuServiceTests {
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final MenuMapper mapper = mock(MenuMapper.class);
    private final AppMapper appMapper = mock(AppMapper.class);
    private final FeatureMapper featureMapper = mock(FeatureMapper.class);
    private final PermissionMapper permissionMapper = mock(PermissionMapper.class);
    private final MenuTxService txService = mock(MenuTxService.class);
    private final MenuConverter converter = mock(MenuConverter.class);
    private final MenuService service =
            new MenuService(currentUserContext, mapper, appMapper, featureMapper, permissionMapper, txService, converter);

    @Test
    void detailAssemblesReferenceObjectsForEditForm() {
        MenuEntity entity = menu(101L, 20L, 100L, MenuLevelEnum.PAGE, "用户", "/sys/base/user");
        entity.setPermissionId(30L);
        MenuEntity parent = menu(100L, 20L, 0L, MenuLevelEnum.CATEGORY, "基础设置", null);
        parent.setNumber("base");
        AppEntity app = new AppEntity();
        app.setId(20L);
        app.setNumber("sys");
        app.setName("系统管理");
        PermissionEntity permission = new PermissionEntity();
        permission.setId(30L);
        permission.setNumber("sys:user:view");
        permission.setName("查看用户");

        when(mapper.selectById(101L)).thenReturn(entity);
        when(mapper.selectById(100L)).thenReturn(parent);
        when(appMapper.selectById(20L)).thenReturn(app);
        when(permissionMapper.selectById(30L)).thenReturn(permission);
        when(converter.toDetailVO(entity)).thenReturn(new MenuDetailVO());

        MenuDetailVO result = service.detail(101L);

        assertEquals("系统管理", result.getApp().getName());
        assertEquals("基础设置", result.getParent().getName());
        assertEquals("查看用户", result.getPermission().getName());
    }

    @Test
    void treeListReturnsMatchedPageWithParentGroupAndAppName() {
        AppEntity app = new AppEntity();
        app.setId(20L);
        app.setName("系统管理");
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
        assertEquals("系统管理", result.getFirst().getAppName());
        assertEquals(1, result.getFirst().getChildren().size());
        assertEquals("用户", result.getFirst().getChildren().getFirst().getName());
    }

    @Test
    void treeListReturnsRootPageWithoutSyntheticGroup() {
        AppEntity app = new AppEntity();
        app.setId(32L);
        app.setName("任务调度");
        when(appMapper.selectList(any())).thenReturn(List.of(app));

        MenuEntity page = menu(201L, 32L, 0L, MenuLevelEnum.PAGE, "定时任务", "/sys/scheduler/job");
        when(mapper.selectList(any())).thenReturn(List.of(page));
        when(converter.toTreeVO(page)).thenReturn(toTreeVO(page));

        List<MenuTreeVO> result = service.listTree(new MenuTreeListForm());

        assertEquals(1, result.size());
        assertEquals("定时任务", result.getFirst().getName());
        assertEquals("任务调度", result.getFirst().getAppName());
    }

    @Test
    void userMenuReturnsRootPageWithoutSyntheticGroup() {
        MenuAppInfoVO appInfo = new MenuAppInfoVO();
        appInfo.setAppName("采购管理");
        appInfo.setAppNumber("procurement");
        appInfo.setCloudNumber("scm");
        MenuEntity page = menu(
                301L, 430000000000000002L, 0L, MenuLevelEnum.PAGE, "采购申请",
                "/scm/procurement/purchase-requisition");
        when(mapper.selectAppInfo(430000000000000002L)).thenReturn(appInfo);
        when(mapper.selectUserMenus(any(), any(), any(), anyBoolean())).thenReturn(List.of(page));

        MenuVO result = service.getUserMenusByAppId(1L, 430000000000000002L);

        assertEquals(1, result.getRoutes().size());
        assertEquals("采购申请", result.getRoutes().getFirst().getName());
    }

    @Test
    void userMenuReturnsExternalLinkNavigationFields() {
        MenuEntity page = menu(
                401L, 31L, 0L, MenuLevelEnum.PAGE, "外部首页", null);
        page.setTargetType(MenuTargetTypeEnum.EXTERNAL_LINK);
        page.setExternalUrl("https://x.com/home");
        page.setExternalOpenMode(ExternalOpenModeEnum.NEW_TAB);
        when(mapper.selectUserMenus(any(), any(), any(), anyBoolean())).thenReturn(List.of(page));

        MenuVO result = service.getUserMenusByAppId(1L, 31L);

        MenuVO externalMenu = result.getRoutes().getFirst();
        assertEquals(401L, externalMenu.getId());
        assertEquals(MenuTargetTypeEnum.EXTERNAL_LINK, externalMenu.getTargetType());
        assertEquals("https://x.com/home", externalMenu.getExternalUrl());
        assertEquals(ExternalOpenModeEnum.NEW_TAB, externalMenu.getExternalOpenMode());
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
