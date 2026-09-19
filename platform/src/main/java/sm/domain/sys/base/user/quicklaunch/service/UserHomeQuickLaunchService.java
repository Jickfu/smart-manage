package sm.domain.sys.base.user.quicklaunch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.app.model.vo.AppVO;
import sm.domain.sys.base.app.model.vo.DomainAppsVO;
import sm.domain.sys.base.app.service.AppService;
import sm.domain.sys.base.menu.model.vo.MenuVO;
import sm.domain.sys.base.menu.service.MenuService;
import sm.domain.sys.base.user.quicklaunch.mapper.UserHomeQuickLaunchMapper;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;
import sm.domain.sys.base.user.quicklaunch.model.form.QuickLaunchSaveForm;
import sm.domain.sys.base.user.quicklaunch.model.form.QuickLaunchScopeForm;
import sm.domain.sys.base.user.quicklaunch.model.vo.QuickLaunchConfigurationVO;
import sm.domain.sys.base.user.quicklaunch.model.vo.QuickLaunchItemVO;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.security.context.CurrentUserContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserHomeQuickLaunchService {
    private final CurrentUserContext currentUserContext;
    private final AppService appService;
    private final MenuService menuService;
    private final UserHomeQuickLaunchMapper mapper;
    private final UserHomeQuickLaunchTxService txService;

    public List<QuickLaunchItemVO> list(QuickLaunchScopeForm form) {
        ScopeContext scopeContext = resolveScope(form);
        return mapper.selectCurrentUserItems(
                currentUserContext.getUserId(),
                currentUserContext.getOrgId(),
                currentUserContext.isAdministrator(),
                scopeContext.scope(),
                scopeContext.appId());
    }

    public QuickLaunchConfigurationVO configuration(QuickLaunchScopeForm form) {
        ScopeContext scopeContext = resolveScope(form);
        List<QuickLaunchItemVO> options = accessibleOptions(scopeContext);
        Set<Long> optionIds = new HashSet<>();
        for (QuickLaunchItemVO option : options) {
            optionIds.add(option.getMenuId());
        }
        List<Long> selectedMenuIds = mapper.selectMenuIds(
                        currentUserContext.getUserId(), scopeContext.scope(), scopeContext.appId())
                .stream()
                .filter(optionIds::contains)
                .toList();
        return new QuickLaunchConfigurationVO(options, selectedMenuIds);
    }

    public void save(QuickLaunchSaveForm form) {
        ScopeContext scopeContext = resolveScope(form);
        Map<Long, QuickLaunchItemVO> optionMap = new LinkedHashMap<>();
        for (QuickLaunchItemVO option : accessibleOptions(scopeContext)) {
            optionMap.put(option.getMenuId(), option);
        }

        List<Long> normalizedMenuIds = new ArrayList<>();
        Set<Long> uniqueMenuIds = new HashSet<>();
        for (Long menuId : form.getMenuIds()) {
            if (menuId == null || !optionMap.containsKey(menuId)) {
                throw new BizException(ResultEnum.PERMISSION_ERROR, "快捷菜单不存在或当前账号无权访问");
            }
            if (!uniqueMenuIds.add(menuId)) {
                throw new BizException(ResultEnum.PARAM_ERROR, "快捷菜单不能重复");
            }
            normalizedMenuIds.add(menuId);
        }
        txService.replace(
                currentUserContext.getUserId(), scopeContext.scope(), scopeContext.appId(), normalizedMenuIds);
    }

    private ScopeContext resolveScope(QuickLaunchScopeForm form) {
        if (HomeScopeEnum.SYSTEM.equals(form.getScope())) {
            if (form.getAppNumber() != null && !form.getAppNumber().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "系统首页不能指定应用");
            }
            return new ScopeContext(HomeScopeEnum.SYSTEM, null, null);
        }
        if (!HomeScopeEnum.APPLICATION.equals(form.getScope())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "首页范围无效");
        }
        String appNumber = form.getAppNumber() == null ? "" : form.getAppNumber().trim();
        if (appNumber.isEmpty()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "应用首页必须指定应用");
        }
        AppVO app = appService.getUserAppByNumber(currentUserContext.getUserId(), appNumber);
        return new ScopeContext(HomeScopeEnum.APPLICATION, app.getId(), app);
    }

    private List<QuickLaunchItemVO> accessibleOptions(ScopeContext scopeContext) {
        List<AppVO> applications = new ArrayList<>();
        if (scopeContext.application() != null) {
            applications.add(scopeContext.application());
        } else {
            for (DomainAppsVO domain : appService.getUserDomainApps(currentUserContext.getUserId())) {
                applications.addAll(domain.getAppList());
            }
        }

        List<QuickLaunchItemVO> options = new ArrayList<>();
        for (AppVO application : applications) {
            MenuVO menuRoot = menuService.getUserMenusByAppNumber(
                    currentUserContext.getUserId(), application.getNumber());
            appendMenuOptions(options, application, null, menuRoot.getRoutes());
        }
        return options;
    }

    private void appendMenuOptions(
            List<QuickLaunchItemVO> options,
            AppVO application,
            String groupName,
            List<MenuVO> menus) {
        if (menus == null) return;
        for (MenuVO menu : menus) {
            if (menu.getRoutes() != null && !menu.getRoutes().isEmpty()) {
                appendMenuOptions(options, application, menu.getName(), menu.getRoutes());
                continue;
            }
            if (menu.getId() == null || menu.getTargetType() == null) continue;
            QuickLaunchItemVO option = new QuickLaunchItemVO();
            option.setMenuId(menu.getId());
            option.setMenuNumber(menu.getNumber());
            option.setName(menu.getName());
            option.setIcon(menu.getIcon());
            option.setAppNumber(application.getNumber());
            option.setAppName(application.getName());
            option.setGroupName(groupName);
            option.setComponent(menu.getComponent());
            option.setTargetType(menu.getTargetType());
            option.setExternalUrl(menu.getExternalUrl());
            option.setExternalOpenMode(menu.getExternalOpenMode());
            options.add(option);
        }
    }

    private record ScopeContext(HomeScopeEnum scope, Long appId, AppVO application) {
    }
}
