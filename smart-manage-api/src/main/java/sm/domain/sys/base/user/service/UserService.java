package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.base.common.helper.UserHelper;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.menu.service.MenuService;
import sm.domain.sys.base.permission.service.PermissionService;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserListForm;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignForm;
import sm.domain.sys.base.user.model.vo.UserCreateNewDataVO;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.domain.sys.base.user.model.vo.UserListVO;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import sm.system.helper.Argon2Helper;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
	private final UserMapper mapper;
	private final UserRoleMapper userRoleMapper;
	private final UserTxService txService;
	private final MenuService menuService;
	private final PermissionService permissionService;
	private final AuthorizationStateHelper authorizationStateHelper;
	private final UserConverter converter;

	@Value("${smart-manage.org.default-id:1}")
	private Long defaultOrgId;

	public PageData<UserListVO> listPage(UserListForm form) {
		LambdaQueryWrapper<UserEntity> qw = new LambdaQueryWrapper<UserEntity>().orderByAsc(UserEntity::getId);
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String kw = form.getKeyword().trim();
			qw.and(condition -> condition.like(UserEntity::getUsername, kw).or().like(UserEntity::getNickname, kw));
		}
		Page<UserEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<UserEntity> result = mapper.selectPage(page, qw);
		var vos = result.getRecords().stream().map(converter::toListVO).collect(Collectors.toList());
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

	@BizLog("保存用户")
	@CacheInvalidate(name = CacheConstant.USER_INFO, key = "#form.id", condition = "#form.id != null")
	public Long save(UserSaveForm form) {
		return txService.save(form);
	}

	@BizLog("删除用户")
	@CacheInvalidate(name = CacheConstant.USER_INFO, key = "#id")
	public void deleteById(Long id) {
		txService.deleteById(id);
	}

	@BizLog("启用用户")
	public void enable(List<Long> ids) {
		txService.updateEnabled(ids, true);
		authorizationStateHelper.invalidateUsers(ids);
	}

	@BizLog("禁用用户")
	public void disable(List<Long> ids) {
		txService.updateEnabled(ids, false);
		authorizationStateHelper.invalidateUsers(ids);
	}

	@BizLog("分配用户角色")
	public void assignRoles(UserRoleAssignForm form) {
		txService.assignRoles(form);
		authorizationStateHelper.invalidateUsers(List.of(form.getUserId()));
	}

	/** 查询用户及当前组织下的角色明细。 */
	public UserInfoVO detail(Long id) {
		UserEntity userEntity = mapper.selectById(id);
		if (userEntity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
		}
		UserInfoVO userInfoVO = converter.toInfoVO(userEntity);
		userInfoVO.setRoleIds(userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
					.select(UserRoleEntity::getRoleId)
					.eq(UserRoleEntity::getUserId, id)
					.eq(UserRoleEntity::getOrgId, UserHelper.getCurrentOrgId()))
				.stream()
				.map(UserRoleEntity::getRoleId)
				.toList());
		return userInfoVO;
	}

	public LoginVO login(String username, String password) {
		// 查询用户
		UserEntity user = mapper.selectOne(
				new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
		if (user == null) {
			return new LoginVO("用户名或密码错误");
		}

		// 使用 Argon2 验证密码
		if (!Argon2Helper.verify(user.getPassword(), password)) {
			return new LoginVO("用户名或密码错误");
		}

		// 检查用户状态
		if (user.getEnabled() == null || !user.getEnabled()) {
			return new LoginVO("用户已被禁用");
		}

		// 登录成功，使用 Sa-Token 登录
		StpUtil.login(user.getId());
		// 默认组织，供权限与业务按组织维度使用
		UserHelper.setCurrentOrgId(defaultOrgId);
		String token = StpUtil.getTokenValue();

		LoginVO vo = new LoginVO();
		vo.setToken(token);
		vo.setNickname(user.getNickname());
		vo.setAccess(UserConstant.SUPER_ADMIN.equalsIgnoreCase(user.getUsername()) ? "kdcloud" : "");
		return vo;
	}

	public UserInfoVO current() {
		// 直接走 mapper，避免自调用绕过缓存代理
		UserEntity userEntity = mapper.selectById(UserHelper.getCurrentUserId());
		return converter.toInfoVO(userEntity);
	}

	@BizLog("修改个人主题")
	@CacheInvalidate(name = CacheConstant.USER_INFO, key = "T(sm.domain.sys.base.common.helper.UserHelper).getCurrentUserId()")
	public void updateCurrentTheme(String themeColor) {
		txService.updateCurrentTheme(UserHelper.getCurrentUserId(), themeColor);
	}

	/**
	 * 按前缀获取当前用户的权限编码列表
	 */
	public List<String> permissions(String prefix) {
		if (UserHelper.isAdmin()) {
			return List.of("*");
		}
		return permissionService.getUserPermissionsByPrefix(UserHelper.getCurrentUserId(), UserHelper.getCurrentOrgId(), prefix);
	}

	/** Redis 远程缓存读取（外部调用时走代理生效，内部调用请直接使用 mapper） */
	@Cached(cacheType = CacheType.REMOTE, name = CacheConstant.USER_INFO, key = "#id", expire = 1, timeUnit = TimeUnit.HOURS)
	public UserEntity requireUser(Long id) {
		UserEntity entity = mapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
		}
		return entity;
	}

	/**
	 * 获取用户新增默认值
	 */
	public UserCreateNewDataVO createNewData() {
		UserCreateNewDataVO vo = new UserCreateNewDataVO();
		// 默认组织ID
		vo.setDefaultOrgId(defaultOrgId);
		// 默认启用
		vo.setEnabled(true);
		// 可根据业务需要设置默认角色等
		return vo;
	}
}
