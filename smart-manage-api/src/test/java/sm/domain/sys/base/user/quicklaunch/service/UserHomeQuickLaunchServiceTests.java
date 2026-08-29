package sm.domain.sys.base.user.quicklaunch.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.app.model.vo.AppVO;
import sm.domain.sys.base.app.service.AppService;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;
import sm.domain.sys.base.menu.model.vo.MenuVO;
import sm.domain.sys.base.menu.service.MenuService;
import sm.domain.sys.base.user.quicklaunch.mapper.UserHomeQuickLaunchMapper;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;
import sm.domain.sys.base.user.quicklaunch.model.form.QuickLaunchSaveForm;
import sm.domain.sys.base.user.quicklaunch.model.form.QuickLaunchScopeForm;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserHomeQuickLaunchServiceTests {
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final AppService appService = mock(AppService.class);
    private final MenuService menuService = mock(MenuService.class);
    private final UserHomeQuickLaunchMapper mapper = mock(UserHomeQuickLaunchMapper.class);
    private final UserHomeQuickLaunchTxService txService = mock(UserHomeQuickLaunchTxService.class);
    private final UserHomeQuickLaunchService service = new UserHomeQuickLaunchService(
            currentUserContext, appService, menuService, mapper, txService);

    @Test
    void applicationListResolvesCurrentUsersAccessibleApplication() {
        when(currentUserContext.getUserId()).thenReturn(10L);
        when(currentUserContext.getOrgId()).thenReturn(30L);
        AppVO application = application(20L, "base", "系统管理");
        when(appService.getUserAppByNumber(10L, "base")).thenReturn(application);
        QuickLaunchScopeForm form = applicationScope(" base ");

        service.list(form);

        verify(mapper).selectCurrentUserItems(10L, 30L, false, HomeScopeEnum.APPLICATION, 20L);
    }

    @Test
    void systemScopeRejectsApplicationNumber() {
        QuickLaunchScopeForm form = new QuickLaunchScopeForm();
        form.setScope(HomeScopeEnum.SYSTEM);
        form.setAppNumber("base");

        assertThrows(BizException.class, () -> service.list(form));
    }

    @Test
    void saveRejectsMenuOutsideCurrentAuthorizationOptions() {
        when(currentUserContext.getUserId()).thenReturn(10L);
        AppVO application = application(20L, "base", "系统管理");
        when(appService.getUserAppByNumber(10L, "base")).thenReturn(application);
        when(menuService.getUserMenusByAppNumber(10L, "base")).thenReturn(menuRoot(page(100L)));
        QuickLaunchSaveForm form = new QuickLaunchSaveForm();
        form.setScope(HomeScopeEnum.APPLICATION);
        form.setAppNumber("base");
        form.setMenuIds(List.of(999L));

        assertThrows(BizException.class, () -> service.save(form));
    }

    @Test
    void savePreservesSelectedMenuOrder() {
        when(currentUserContext.getUserId()).thenReturn(10L);
        AppVO application = application(20L, "base", "系统管理");
        when(appService.getUserAppByNumber(10L, "base")).thenReturn(application);
        when(menuService.getUserMenusByAppNumber(10L, "base"))
                .thenReturn(menuRoot(page(100L), page(101L)));
        QuickLaunchSaveForm form = new QuickLaunchSaveForm();
        form.setScope(HomeScopeEnum.APPLICATION);
        form.setAppNumber("base");
        form.setMenuIds(List.of(101L, 100L));

        service.save(form);

        verify(txService).replace(10L, HomeScopeEnum.APPLICATION, 20L, List.of(101L, 100L));
    }

    private QuickLaunchScopeForm applicationScope(String appNumber) {
        QuickLaunchScopeForm form = new QuickLaunchScopeForm();
        form.setScope(HomeScopeEnum.APPLICATION);
        form.setAppNumber(appNumber);
        return form;
    }

    private AppVO application(Long id, String number, String name) {
        AppVO application = new AppVO();
        application.setId(id);
        application.setNumber(number);
        application.setName(name);
        return application;
    }

    private MenuVO menuRoot(MenuVO... pages) {
        MenuVO root = new MenuVO();
        root.setRoutes(List.of(pages));
        return root;
    }

    private MenuVO page(Long id) {
        MenuVO page = new MenuVO();
        page.setId(id);
        page.setNumber("menu-" + id);
        page.setName("菜单" + id);
        page.setLevel(MenuLevelEnum.PAGE);
        page.setTargetType(MenuTargetTypeEnum.INTERNAL_PAGE);
        page.setComponent("sys/base/example");
        return page;
    }
}
