package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.base.common.config.OrgConfig;
import sm.domain.sys.base.common.helper.CurrentUserContext;
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
import sm.domain.sys.base.user.model.vo.ResetPasswordVO;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
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
	private final CurrentUserContext currentUserContext;
	private final OrgConfig orgConfig;

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
					.eq(UserRoleEntity::getOrgId, currentUserContext.getOrgId()))
				.stream()
				.map(UserRoleEntity::getRoleId)
				.toList());
		return userInfoVO;
	}

	public UserAuthentication authenticate(String username, String password) {
		// 查询用户
		UserEntity user = mapper.selectOne(
				new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
		if (user == null) {
			return UserAuthentication.failed("用户名或密码错误");
		}

		// 使用 Argon2 验证密码
		if (!Argon2Helper.verify(user.getPassword(), password)) {
			return UserAuthentication.failed("用户名或密码错误");
		}

		// 检查用户状态
		if (user.getEnabled() == null || !user.getEnabled()) {
			return UserAuthentication.failed("用户已被禁用");
		}

		return new UserAuthentication(
				user.getId(),
				user.getUsername(),
				user.getNickname(),
				Boolean.TRUE.equals(user.getPasswordReset()),
				UserConstant.SUPER_ADMIN.equals(user.getUsername()),
				null);
	}

	/** 凭据验证且无需强制改密后，才创建正式登录状态。 */
	public LoginVO completeLogin(UserAuthentication authentication) {
		StpUtil.login(authentication.userId());
		currentUserContext.initializeIdentity(
				authentication.username(), authentication.administrator());
		String token = StpUtil.getTokenValue();

		LoginVO vo = new LoginVO();
		vo.setToken(token);
		vo.setNickname(authentication.nickname());
		vo.setAccess(authentication.administrator() ? "kdcloud" : "");
		return vo;
	}

	@BizLog(value = "重置用户密码", recordResponse = false)
	public ResetPasswordVO resetPassword(Long userId) {
		String password = txService.resetPassword(userId);
		authorizationStateHelper.invalidateUsers(List.of(userId));
		return new ResetPasswordVO(password);
	}

	public void changeResetPassword(Long userId, String newPassword) {
		if (newPassword == null || newPassword.isBlank()) {
			throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能为空");
		}
		txService.changeResetPassword(userId, newPassword);
		authorizationStateHelper.invalidateUsers(List.of(userId));
	}

	public UserInfoVO current() {
		// 直接走 mapper，避免自调用绕过缓存代理
		UserEntity userEntity = mapper.selectById(currentUserContext.getUserId());
		return converter.toInfoVO(userEntity);
	}

	@BizLog("修改个人主题")
	@CacheInvalidate(name = CacheConstant.USER_INFO, key = "@currentUserContext.getUserId()")
	public void updateCurrentTheme(String themeColor) {
		txService.updateCurrentTheme(currentUserContext.getUserId(), themeColor);
	}

	/**
	 * 按前缀获取当前用户的权限编码列表
	 */
	public List<String> permissions(String prefix) {
		if (currentUserContext.isAdministrator()) {
			return List.of("*");
		}
		return permissionService.getUserPermissionsByPrefix(currentUserContext.getUserId(), currentUserContext.getOrgId(), prefix);
	}

	/** Redis 远程缓存读取；仅供其他 Spring Bean 外部调用，确保缓存代理生效。 */
	@Cached(cacheType = CacheType.REMOTE, name = CacheConstant.USER_INFO,
			key = "#id", expire = 1, timeUnit = TimeUnit.HOURS)
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
		vo.setDefaultOrgId(orgConfig.getDefaultId());
		// 默认启用
		vo.setEnabled(true);
		// 可根据业务需要设置默认角色等
		return vo;
	}
}
