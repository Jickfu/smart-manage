package sm.domain.sys.base.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.domain.sys.base.app.mapper.AppMapper;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuListForm;
import sm.domain.sys.base.menu.model.form.MenuSaveForm;
import sm.domain.sys.base.menu.model.form.MenuSelectForm;
import sm.domain.sys.base.menu.model.form.MenuTreeListForm;
import sm.domain.sys.base.menu.model.vo.*;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListSqlQuery;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {
	private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
			"number", ListSqlQuery.string("number", false),
			"name", ListSqlQuery.string("name", false),
			"level", ListSqlQuery.enumeration("level", false),
			"path", ListSqlQuery.string("path", false),
			"component", ListSqlQuery.string("component", false),
			"sort", ListSqlQuery.number("sort", false),
			"enabled", ListSqlQuery.bool("enabled", false));
	private final CurrentUserContext currentUserContext;
	private final MenuMapper mapper;
	private final AppMapper appMapper;
	private final FeatureMapper featureMapper;
	private final PermissionMapper permissionMapper;
	private final MenuTxService txService;
	private final MenuConverter converter;

	/**
	 * 前端工作区白名单 key：与路径一致（去前导 /、全小写），例如 "/sys/monitor/home" -> "sys/monitor/home"
	 */
	private static String toWorkspaceComponentKeyByPath(String path) {
		if (path == null) {
			return null;
		}
		String p = path.trim();
		if (p.isEmpty()) {
			return null;
		}
		if (p.startsWith("/")) {
			p = p.substring(1);
		}
		if (p.isEmpty()) {
			return null;
		}
		return p.toLowerCase();
	}

	public PageData<MenuListVO> listPage(MenuListForm form) {
		LambdaQueryWrapper<MenuEntity> qw = new LambdaQueryWrapper<MenuEntity>();
		qw.eq(form.getAppId() != null, MenuEntity::getAppId, form.getAppId());
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String keyword = form.getKeyword().trim();
			qw.and(condition -> condition.like(MenuEntity::getName, keyword)
					.or().like(MenuEntity::getPath, keyword));
		}
		qw.orderByAsc(MenuEntity::getSort).orderByAsc(MenuEntity::getId);
		Page<MenuEntity> result = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), qw);
		List<MenuListVO> records = result.getRecords().stream().map(converter::toListVO).collect(Collectors.toList());
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
	}

	/**
	 * 获取指定应用下的完整菜单集合，供菜单管理左侧树一次性构建层级。
	 */
	public List<MenuTreeVO> listByApp(Long appId) {
		List<MenuEntity> entityList = mapper.selectList(new LambdaQueryWrapper<MenuEntity>()
				.eq(MenuEntity::getAppId, appId)
				.orderByAsc(MenuEntity::getLevel)
				.orderByAsc(MenuEntity::getSort)
				.orderByAsc(MenuEntity::getId));
		return entityList.stream().map(converter::toTreeVO).toList();
	}

	/**
	 * 获取菜单管理树形列表。先限定云/应用范围，再一次性组装分组与页面，避免分页切断父子关系。
	 */
	public List<MenuTreeVO> listTree(MenuTreeListForm form) {
		LambdaQueryWrapper<AppEntity> appWrapper = new LambdaQueryWrapper<AppEntity>()
				.eq(form.getAppId() != null, AppEntity::getId, form.getAppId())
				.eq(form.getAppId() == null && form.getCloudId() != null,
						AppEntity::getCloudId, form.getCloudId())
				.orderByAsc(AppEntity::getSeq)
				.orderByAsc(AppEntity::getId);
		List<AppEntity> apps = appMapper.selectList(appWrapper);
		if (apps.isEmpty()) {
			return List.of();
		}

		List<Long> appIds = new ArrayList<>();
		Map<Long, String> appNames = new HashMap<>();
		for (AppEntity app : apps) {
			appIds.add(app.getId());
			appNames.put(app.getId(), app.getName());
		}

		List<MenuEntity> menus = mapper.selectList(new LambdaQueryWrapper<MenuEntity>()
				.in(MenuEntity::getAppId, appIds)
				.orderByAsc(MenuEntity::getAppId)
				.orderByAsc(MenuEntity::getLevel)
				.orderByAsc(MenuEntity::getSort)
				.orderByAsc(MenuEntity::getId));
		menus = filterTreeMenus(menus, form.getKeyword(), ListSqlQuery.of(form, LIST_FIELDS));
		return assembleMenuTree(menus, appNames);
	}

	/** 关键词命中页面时保留其父分组，保证返回结果始终可以组成完整层级。 */
	private List<MenuEntity> filterTreeMenus(List<MenuEntity> menus, String keyword, ListSqlQuery listQuery) {
		if ((keyword == null || keyword.isBlank()) && listQuery.conditions().isEmpty()) {
			return menus;
		}
		String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
		Set<Long> includedIds = new HashSet<>();
		for (MenuEntity menu : menus) {
			String name = menu.getName() == null ? "" : menu.getName().toLowerCase(Locale.ROOT);
			String path = menu.getPath() == null ? "" : menu.getPath().toLowerCase(Locale.ROOT);
			boolean keywordMatches = normalizedKeyword.isEmpty()
					|| name.contains(normalizedKeyword) || path.contains(normalizedKeyword);
			if (keywordMatches && listQuery.conditions().stream().allMatch(condition -> matches(menu, condition))) {
				includedIds.add(menu.getId());
				if (MenuLevelEnum.PAGE.equals(menu.getLevel())
						&& menu.getParentId() != null && menu.getParentId() > 0) {
					includedIds.add(menu.getParentId());
				}
			}
		}
		List<MenuEntity> filteredMenus = new ArrayList<>();
		for (MenuEntity menu : menus) {
			if (includedIds.contains(menu.getId())) {
				filteredMenus.add(menu);
			}
		}
		return filteredMenus;
	}

	private boolean matches(MenuEntity menu, ListSqlQuery.Condition condition) {
		Object rawValue = switch (condition.column()) {
			case "number" -> menu.getNumber();
			case "name" -> menu.getName();
			case "level" -> menu.getLevel();
			case "path" -> menu.getPath();
			case "component" -> menu.getComponent();
			case "sort" -> menu.getSort();
			case "enabled" -> menu.getEnabled();
			default -> null;
		};
		String text = rawValue == null ? "" : String.valueOf(rawValue);
		String expected = condition.value() == null ? "" : String.valueOf(condition.value());
		int comparison = rawValue instanceof Number && condition.value() instanceof Number
				? new java.math.BigDecimal(text).compareTo((java.math.BigDecimal) condition.value())
				: text.compareTo(expected);
		return switch (condition.operator()) {
			case EMPTY -> rawValue == null || text.isEmpty();
			case NOT_EMPTY -> rawValue != null && !text.isEmpty();
			case IN -> condition.values().stream().anyMatch(value -> String.valueOf(value).equals(text));
			case CONTAINS -> text.contains(expected);
			case NOT_CONTAINS -> !text.contains(expected);
			case STARTS_WITH -> text.startsWith(expected);
			case ENDS_WITH -> text.endsWith(expected);
			case EQ -> comparison == 0;
			case NE -> comparison != 0;
			case GT -> comparison > 0;
			case GE -> comparison >= 0;
			case LT -> comparison < 0;
			case LE -> comparison <= 0;
			default -> false;
		};
	}

	/** 按分组、页面两层结构组装 Ant Design Table 可直接消费的 children 数据。 */
	private List<MenuTreeVO> assembleMenuTree(List<MenuEntity> menus, Map<Long, String> appNames) {
		List<MenuTreeVO> roots = new ArrayList<>();
		Map<Long, MenuTreeVO> groups = new HashMap<>();
		List<MenuTreeVO> pages = new ArrayList<>();
		for (MenuEntity menu : menus) {
			MenuTreeVO node = converter.toTreeVO(menu);
			node.setAppName(appNames.get(menu.getAppId()));
			if (MenuLevelEnum.CATEGORY.equals(menu.getLevel())) {
				node.setChildren(new ArrayList<>());
				roots.add(node);
				groups.put(menu.getId(), node);
			} else if (MenuLevelEnum.PAGE.equals(menu.getLevel())) {
				pages.add(node);
			} else {
				throw new BizException(ResultEnum.CONFIG_ERROR, "菜单层级配置无效：" + menu.getId());
			}
		}

		for (MenuTreeVO page : pages) {
			if (page.getParentId() == null || page.getParentId() == 0) {
				roots.add(page);
				continue;
			}
			MenuTreeVO parent = groups.get(page.getParentId());
			if (parent == null) {
				throw new BizException(ResultEnum.CONFIG_ERROR, "页面菜单缺少父分组：" + page.getId());
			}
			parent.getChildren().add(page);
		}
		// 查询为保证父分组先于子页面按层级排序，组装完成后再统一恢复根节点业务排序。
		roots.sort(Comparator.comparing(MenuTreeVO::getSort, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(MenuTreeVO::getId));
		return roots;
	}

	/**
	 * 基础资料选择：分页查询菜单。
	 * 支持按应用、层级、排除自身、是否启用、关键词过滤；按 sort、id 排序。
	 */
	public PageData<MenuSelectVO> select(MenuSelectForm form) {
		LambdaQueryWrapper<MenuEntity> wrapper = new LambdaQueryWrapper<MenuEntity>();
		wrapper.eq(form.getAppId() != null, MenuEntity::getAppId, form.getAppId())
				.eq(form.getLevel() != null, MenuEntity::getLevel, form.getLevel())
				.ne(form.getExcludeId() != null, MenuEntity::getId, form.getExcludeId())
				.eq(form.getEnabled() != null, MenuEntity::getEnabled, form.getEnabled());
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String keyword = form.getKeyword().trim();
			wrapper.and(condition -> condition.like(MenuEntity::getNumber, keyword)
					.or().like(MenuEntity::getName, keyword));
		}
		wrapper.orderByAsc(MenuEntity::getSort).orderByAsc(MenuEntity::getId);
		Page<MenuEntity> result = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), wrapper);
		List<MenuSelectVO> records = result.getRecords().stream().map(converter::toSelectVO).collect(Collectors.toList());
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
	}


	/**
	 * 获取应用下的菜单
	 */
	//	@Cacheable(cacheNames = "user_menu", key = "#userId + ':' + #appId")
	public MenuVO getUserMenusByAppId(Long userId, Long appId) {
		MenuVO root = new MenuVO();
		root.setRoutes(new ArrayList<>());
		if (userId == null || appId == null) {
			return root;
		}

		MenuAppInfoVO appInfo = mapper.selectAppInfo(appId);
		if (appInfo != null) {
			root.setName(appInfo.getAppName());
			root.setIcon(appInfo.getAppIcon());
			if (appInfo.getCloudNumber() != null && !appInfo.getCloudNumber().isBlank()
					&& appInfo.getAppNumber() != null && !appInfo.getAppNumber().isBlank()) {
				root.setPath("/" + appInfo.getCloudNumber() + "/" + appInfo.getAppNumber() + "/home");
				root.setComponent(toWorkspaceComponentKeyByPath(root.getPath()));
			}
		}

		List<MenuEntity> entityList = mapper.selectUserMenus(
				userId, currentUserContext.getOrgId(), appId, currentUserContext.isAdministrator());
		Map<Long, MenuVO> categories = new HashMap<>();
		Map<MenuVO, Integer> rootSorts = new IdentityHashMap<>();
		for (MenuEntity menuEntity : entityList) {
			MenuVO menu = new MenuVO();
			menu.setName(menuEntity.getName());
			menu.setPath(menuEntity.getPath());
			menu.setComponent(menuEntity.getComponent());
			menu.setIcon(menuEntity.getIcon());
			menu.setLevel(menuEntity.getLevel());
			if (MenuLevelEnum.CATEGORY.equals(menuEntity.getLevel())) {
				root.getRoutes().add(menu);
				rootSorts.put(menu, menuEntity.getSort());
				categories.put(menuEntity.getId(), menu);
			} else if (MenuLevelEnum.PAGE.equals(menuEntity.getLevel())) {
				if (menuEntity.getParentId() == null || menuEntity.getParentId() == 0) {
					root.getRoutes().add(menu);
					rootSorts.put(menu, menuEntity.getSort());
					continue;
				}
				MenuVO parent = categories.get(menuEntity.getParentId());
				if (parent != null) {
					if (parent.getRoutes() == null) {
						parent.setRoutes(new ArrayList<>());
					}
					parent.getRoutes().add(menu);
				}
			}
		}
		root.getRoutes().sort(Comparator.comparing(
				rootSorts::get, Comparator.nullsLast(Integer::compareTo)));
		return root;
	}

	/**
	 * 按应用编号（t_sys_app.number）获取当前用户在应用下的菜单树。
	 * 返回空 root（routes=[]）表示应用不存在或无权限菜单。
	 */
	public MenuVO getUserMenusByAppNumber(Long userId, String appNumber) {
		if (appNumber == null || appNumber.isBlank()) {
			MenuVO empty = new MenuVO();
			empty.setRoutes(new ArrayList<>());
			return empty;
		}
		AppEntity app = appMapper.selectOne(new LambdaQueryWrapper<AppEntity>()
				.eq(AppEntity::getNumber, appNumber)
				.eq(AppEntity::getEnabled, true));
		Long appId = app == null ? null : app.getId();
		if (appId == null) {
			MenuVO empty = new MenuVO();
			empty.setRoutes(new ArrayList<>());
			return empty;
		}
		return getUserMenusByAppId(userId, appId);
	}

	public MenuDetailVO detail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "菜单ID不能为空");
		}
		MenuEntity entity = mapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "菜单不存在");
		}
		MenuDetailVO vo = converter.toDetailVO(entity);
		AppEntity appEntity = appMapper.selectById(entity.getAppId());
		if (appEntity != null) {
			vo.setApp(toReferenceInfo(appEntity.getId(), appEntity.getNumber(), appEntity.getName()));
		}
		FeatureEntity featureEntity = featureMapper.selectById(entity.getFeatureId());
		if (featureEntity != null) {
			String featureName = featureEntity.getCustomName() == null || featureEntity.getCustomName().isBlank()
					? featureEntity.getDefaultName() : featureEntity.getCustomName();
			vo.setFeature(toReferenceInfo(featureEntity.getId(), featureEntity.getFeatureKey(), featureName));
		}
		// 引用控件需要完整的标识、编码和名称，不能只返回外键 ID。
		if (entity.getParentId() != null && entity.getParentId() > 0) {
			MenuEntity parentEntity = mapper.selectById(entity.getParentId());
			if (parentEntity != null) {
				vo.setParent(toReferenceInfo(
						parentEntity.getId(), parentEntity.getNumber(), parentEntity.getName()));
			}
		}
		if (entity.getPermissionId() != null) {
			PermissionEntity permissionEntity = permissionMapper.selectById(entity.getPermissionId());
			if (permissionEntity != null) {
				vo.setPermission(toReferenceInfo(
						permissionEntity.getId(), permissionEntity.getNumber(), permissionEntity.getName()));
			}
		}
		return vo;
	}

	private MenuDetailVO.ReferenceInfo toReferenceInfo(Long id, String number, String name) {
		MenuDetailVO.ReferenceInfo info = new MenuDetailVO.ReferenceInfo();
		info.setId(id);
		info.setNumber(number);
		info.setName(name);
		return info;
	}

	public MenuCreateNewDataVO createNewData() {
		MenuCreateNewDataVO vo = new MenuCreateNewDataVO();
		vo.setParentId(0L);
		vo.setSort(99);
		vo.setEnabled(true);
		return vo;
	}

	@BizLog("保存菜单")
	public Long save(MenuSaveForm form) {
		return txService.save(form);
	}

	@BizLog("删除菜单")
	public void deleteById(Long id) {
		txService.deleteById(id);
	}

	@BizLog("启用菜单")
	public void enable(List<Long> ids) {
		txService.updateEnabled(ids, true);
	}

	@BizLog("禁用菜单")
	public void disable(List<Long> ids) {
		txService.updateEnabled(ids, false);
	}
}
