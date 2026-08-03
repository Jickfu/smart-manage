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
import sm.domain.sys.base.common.service.CurrentUserService;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuListForm;
import sm.domain.sys.base.menu.model.form.MenuSaveForm;
import sm.domain.sys.base.menu.model.form.MenuSelectForm;
import sm.domain.sys.base.menu.model.vo.*;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MenuService {
	private final CurrentUserContext currentUserContext;
	private final CurrentUserService currentUserService;
	private final MenuMapper mapper;
	private final AppMapper appMapper;
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
				userId, currentUserContext.getOrgId(), appId, currentUserService.isAdministrator());
		Map<Long, MenuVO> categories = new HashMap<>();
		for (MenuEntity menuEntity : entityList) {
			MenuVO menu = new MenuVO();
			menu.setName(menuEntity.getName());
			menu.setPath(menuEntity.getPath());
			menu.setComponent(menuEntity.getComponent());
			menu.setIcon(menuEntity.getIcon());
			menu.setLevel(menuEntity.getLevel());
			if (MenuLevelEnum.CATEGORY.equals(menuEntity.getLevel())) {
				root.getRoutes().add(menu);
				categories.put(menuEntity.getId(), menu);
			} else if (MenuLevelEnum.PAGE.equals(menuEntity.getLevel())) {
				MenuVO parent = categories.get(menuEntity.getParentId());
				if (parent != null) {
					if (parent.getRoutes() == null) {
						parent.setRoutes(new ArrayList<>());
					}
					parent.getRoutes().add(menu);
				}
			}
		}
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
		// 填充父菜单信息
		if (entity.getParentId() != null && entity.getParentId() > 0) {
			MenuEntity parentEntity = mapper.selectById(entity.getParentId());
			if (parentEntity != null) {
				MenuDetailVO.ParentInfo info = new MenuDetailVO.ParentInfo();
				info.setId(parentEntity.getId());
				info.setNumber(parentEntity.getNumber());
				info.setName(parentEntity.getName());
				vo.setParent(info);
			}
		}
		return vo;
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
