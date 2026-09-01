package sm.domain.sys.base.menu.service;

import sm.domain.sys.base.menu.converter.MenuConverter;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.app.mapper.AppMapper;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.domain.sys.base.domain.mapper.DomainMapper;
import sm.domain.sys.base.domain.model.entity.DomainEntity;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuTreeListForm;
import sm.domain.sys.base.menu.model.vo.MenuAppInfoVO;
import sm.domain.sys.base.menu.model.vo.MenuTreeVO;
import sm.domain.sys.base.menu.model.vo.MenuDetailVO;
import sm.domain.sys.base.menu.model.vo.MenuVO;
import sm.domain.sys.base.menu.model.vo.MenuCatalogNodeVO;
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
    private final DomainMapper domainMapper = mock(DomainMapper.class);
    private final FeatureMapper featureMapper = mock(FeatureMapper.class);
    private final PermissionMapper permissionMapper = mock(PermissionMapper.class);
    private final MenuTxService txService = mock(MenuTxService.class);
    private final MenuConverter converter = mock(MenuConverter.class);
    private final MenuService service =
            new MenuService(currentUserContext, mapper,
                    new sm.domain.sys.base.app.service.AppReferenceService(appMapper),
                    new sm.domain.sys.base.domain.service.DomainReferenceService(domainMapper),
                    new sm.domain.sys.base.feature.service.FeatureReferenceService(featureMapper),
                    new sm.domain.sys.base.permission.service.PermissionReferenceService(permissionMapper),
                    txService, converter);

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
        app.setDomainId(10L);
        app.setName("系统管理");
        when(appMapper.selectList(null)).thenReturn(List.of(app));

        MenuEntity group = menu(100L, 20L, 0L, MenuLevelEnum.CATEGORY, "基础设置", null);
        MenuEntity page = menu(101L, 20L, 100L, MenuLevelEnum.PAGE, "用户", "/sys/base/user");
        when(mapper.selectList(any())).thenReturn(List.of(group, page));
        when(converter.toTreeVO(any())).thenAnswer(invocation -> toTreeVO(invocation.getArgument(0)));

        MenuTreeListForm form = new MenuTreeListForm();
        form.setDomainId(10L);
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
        when(appMapper.selectList(null)).thenReturn(List.of(app));

        MenuEntity page = menu(201L, 32L, 0L, MenuLevelEnum.PAGE, "定时任务", "/sys/scheduler/job");
        when(mapper.selectList(any())).thenReturn(List.of(page));
        when(converter.toTreeVO(page)).thenReturn(toTreeVO(page));

        List<MenuTreeVO> result = service.listTree(new MenuTreeListForm());

        assertEquals(1, result.size());
        assertEquals("定时任务", result.getFirst().getName());
        assertEquals("任务调度", result.getFirst().getAppName());
    }

    @Test
    void featureScopeReturnsOnlyFeaturePagesAndTheirParentGroups() {
        AppEntity app = new AppEntity();
        app.setId(31L);
        app.setName("系统建模");
        FeatureEntity selectedFeature = new FeatureEntity();
        selectedFeature.setId(501L);
        selectedFeature.setAppId(31L);
        when(featureMapper.selectById(501L)).thenReturn(selectedFeature);
        when(appMapper.selectList(null)).thenReturn(List.of(app));

        MenuEntity group = menu(100L, 31L, 0L, MenuLevelEnum.CATEGORY, "平台结构", null);
        MenuEntity selectedPage = menu(101L, 31L, 100L, MenuLevelEnum.PAGE, "菜单管理", "/menu");
        selectedPage.setFeatureId(501L);
        MenuEntity unrelatedPage = menu(102L, 31L, 100L, MenuLevelEnum.PAGE, "应用管理", "/app");
        unrelatedPage.setFeatureId(502L);
        when(mapper.selectList(any())).thenReturn(List.of(group, selectedPage, unrelatedPage));
        when(converter.toTreeVO(any())).thenAnswer(invocation -> toTreeVO(invocation.getArgument(0)));
        MenuTreeListForm form = new MenuTreeListForm();
        form.setFeatureId(501L);

        List<MenuTreeVO> result = service.listTree(form);

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getChildren().size());
        assertEquals("菜单管理", result.getFirst().getChildren().getFirst().getName());
    }

    @Test
    void catalogReturnsDomainApplicationAndFeatureHierarchy() {
        DomainEntity domain = new DomainEntity();
        domain.setId(4L);
        domain.setNumber("sys");
        domain.setName("系统服务");
        AppEntity app = new AppEntity();
        app.setId(31L);
        app.setDomainId(4L);
        app.setNumber("base");
        app.setName("系统建模");
        FeatureEntity feature = new FeatureEntity();
        feature.setId(501L);
        feature.setAppId(31L);
        feature.setFeatureKey("sys/base/menu");
        feature.setDefaultName("菜单管理");
        feature.setDefaultSeq(10);
        when(domainMapper.selectList(null)).thenReturn(List.of(domain));
        when(appMapper.selectList(null)).thenReturn(List.of(app));
        when(featureMapper.selectList(null)).thenReturn(List.of(feature));

        List<MenuCatalogNodeVO> result = service.catalog();

        assertEquals("DOMAIN", result.getFirst().getType());
        assertEquals("APPLICATION", result.getFirst().getChildren().getFirst().getType());
        assertEquals("FEATURE", result.getFirst().getChildren().getFirst().getChildren().getFirst().getType());
        assertEquals("菜单管理", result.getFirst().getChildren().getFirst().getChildren().getFirst().getName());
    }

    @Test
    void userMenuReturnsRootPageWithoutSyntheticGroup() {
        MenuAppInfoVO appInfo = new MenuAppInfoVO();
        appInfo.setAppName("采购管理");
        appInfo.setAppNumber("procurement");
        appInfo.setDomainNumber("scm");
        MenuEntity page = menu(
                301L, 430000000000000002L, 0L, MenuLevelEnum.PAGE, "采购申请",
                "/scm/procurement/purchase-requisition");
        when(mapper.selectAppInfo(430000000000000002L)).thenReturn(appInfo);
        when(mapper.selectUserMenus(any(), any(), any(), anyBoolean())).thenReturn(List.of(page));

        MenuVO result = service.getUserMenusByAppId(1L, 430000000000000002L);

        assertEquals(1, result.getRoutes().size());
        assertEquals("menu_301", result.getRoutes().getFirst().getNumber());
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
        entity.setNumber("menu_" + id);
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
