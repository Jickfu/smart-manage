package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpUtil;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.constant.BaseCacheName;
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
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.model.vo.AttachmentVO;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.vo.UserAssignmentVO;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import sm.system.helper.Argon2Helper;
import sm.system.auth.SessionTerminationReason;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.io.IOException;
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
	private final UserAssignmentMapper userAssignmentMapper;
	private final OrgMapper orgMapper;
	private final AttachmentService attachmentService;
	private final UserTxService txService;
	private final MenuService menuService;
	private final PermissionService permissionService;
	private final AuthorizationStateHelper authorizationStateHelper;
	private final UserConverter converter;
	private final CurrentUserContext currentUserContext;

	public PageData<UserListVO> listPage(UserListForm form) {
		List<Long> scopedOrgIds = resolveScopedOrgIds(form);
		if (scopedOrgIds != null && scopedOrgIds.isEmpty()) {
			return PageData.of(0, form.getPageNum(), form.getPageSize(), List.of());
		}
		Page<UserEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		String keyword = form.getKeyword() == null ? null : form.getKeyword().trim();
		Page<UserEntity> result = mapper.selectScopedPage(page, keyword, scopedOrgIds,
				Boolean.TRUE.equals(form.getUnassigned()));
		var vos = result.getRecords().stream().map(converter::toListVO).collect(Collectors.toList());
		for (UserListVO vo : vos) vo.setAvatar(avatarUrl(vo.getId(), vo.getAvatarAttachmentId()));
		assembleAssignments(vos, result.getRecords().stream().map(UserEntity::getId).toList(), scopedOrgIds);
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

	@BizLog("保存用户")
	@CacheInvalidate(name = BaseCacheName.USER_INFO, key = "#form.id", condition = "#form.id != null")
	public Long save(UserSaveForm form) {
		UserEntity previous = form.getId() == null ? null : mapper.selectById(form.getId());
		Long userId = previous == null ? IdWorker.getId() : previous.getId();
		Long temporaryAvatarId = findTemporaryAvatarId(form.getAvatarAttachmentId());
		promoteAvatar(form, userId);
		Long savedId;
		try {
			savedId = txService.save(form, userId);
			deleteReplacedAvatar(previous, form.getAvatarAttachmentId());
		} catch (RuntimeException exception) {
			deleteAvatarForCompensation(temporaryAvatarId);
			throw exception;
		}
		if (form.getAssignments().isEmpty()) {
			authorizationStateHelper.terminateUsers(
					List.of(savedId), SessionTerminationReason.ACCOUNT_DISABLED);
		}
		return savedId;
	}

	@BizLog("删除用户")
	@CacheInvalidate(name = BaseCacheName.USER_INFO, key = "#id")
	public void deleteById(Long id) {
		UserEntity user = mapper.selectById(id);
		txService.deleteById(id);
		if (user != null) deleteAvatarForCompensation(user.getAvatarAttachmentId());
	}

	@BizLog("启用用户")
	public void enable(List<Long> ids) {
		txService.updateEnabled(ids, true);
		authorizationStateHelper.refreshUsers(ids);
	}

	@BizLog("禁用用户")
	public void disable(List<Long> ids) {
		txService.updateEnabled(ids, false);
		authorizationStateHelper.terminateUsers(ids, SessionTerminationReason.ACCOUNT_DISABLED);
	}

	@BizLog("分配用户角色")
	public void assignRoles(UserRoleAssignForm form) {
		txService.assignRoles(form);
		authorizationStateHelper.refreshUsers(List.of(form.getUserId()));
	}

	/** 查询用户及当前组织下的角色明细。 */
	public UserInfoVO detail(Long id) {
		UserEntity userEntity = mapper.selectById(id);
		if (userEntity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
		}
		UserInfoVO userInfoVO = converter.toInfoVO(userEntity);
		userInfoVO.setAvatar(avatarUrl(id, userEntity.getAvatarAttachmentId()));
		userInfoVO.setAssignments(loadAssignments(List.of(id), null).getOrDefault(id, List.of()));
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
		UserAssignmentEntity primaryAssignment = userAssignmentMapper.selectOne(
				new LambdaQueryWrapper<UserAssignmentEntity>()
						.eq(UserAssignmentEntity::getUserId, user.getId())
						.eq(UserAssignmentEntity::getIsPrimary, true));
		if (primaryAssignment == null) {
			return UserAuthentication.failed("用户未配置主职组织，请联系管理员");
		}
		OrgEntity primaryOrganization = orgMapper.selectById(primaryAssignment.getOrgId());
		if (primaryOrganization == null || !Boolean.TRUE.equals(primaryOrganization.getEnabled())
				|| Boolean.TRUE.equals(primaryOrganization.getArchived())) {
			return UserAuthentication.failed("用户主职组织不可用，请联系管理员");
		}

		return new UserAuthentication(
				user.getId(),
				user.getUsername(),
				user.getName(),
				Boolean.TRUE.equals(user.getPasswordReset()),
				UserConstant.SUPER_ADMIN.equals(user.getUsername()),
				primaryOrganization.getId(),
				null);
	}

	/** 凭据验证且无需强制改密后，才创建正式登录状态。 */
	public LoginVO completeLogin(UserAuthentication authentication) {
		StpUtil.login(authentication.userId());
		currentUserContext.initializeIdentity(
				authentication.orgId(), authentication.username(), authentication.administrator());
		String token = StpUtil.getTokenValue();

		LoginVO vo = new LoginVO();
		vo.setToken(token);
		vo.setName(authentication.name());
		vo.setAccess(authentication.administrator() ? "kdcloud" : "");
		return vo;
	}

	private List<Long> resolveScopedOrgIds(UserListForm form) {
		if (Boolean.TRUE.equals(form.getUnassigned()) || form.getOrgId() == null) return null;
		OrgEntity selected = orgMapper.selectById(form.getOrgId());
		if (selected == null) throw new BizException(ResultEnum.NOT_FOUND, "组织不存在");
		if (!Boolean.TRUE.equals(form.getIncludeDescendants())) return List.of(selected.getId());
		String prefix = selected.getNumberPath() + "/";
		return orgMapper.selectList(new LambdaQueryWrapper<OrgEntity>()
				.select(OrgEntity::getId)
				.and(scope -> scope.eq(OrgEntity::getId, selected.getId())
						.or().likeRight(OrgEntity::getNumberPath, prefix)))
				.stream().map(OrgEntity::getId).toList();
	}

	private void assembleAssignments(List<UserListVO> users, List<Long> userIds, List<Long> scopedOrgIds) {
		Map<Long, List<UserAssignmentVO>> assignmentByUser = loadAssignments(userIds, scopedOrgIds);
		for (UserListVO user : users) user.setAssignments(assignmentByUser.getOrDefault(user.getId(), List.of()));
	}

	private Map<Long, List<UserAssignmentVO>> loadAssignments(List<Long> userIds, List<Long> scopedOrgIds) {
		if (userIds.isEmpty()) return Map.of();
		LambdaQueryWrapper<UserAssignmentEntity> query = new LambdaQueryWrapper<UserAssignmentEntity>()
				.in(UserAssignmentEntity::getUserId, userIds)
				.in(scopedOrgIds != null, UserAssignmentEntity::getOrgId, scopedOrgIds)
				.orderByDesc(UserAssignmentEntity::getIsPrimary)
				.orderByAsc(UserAssignmentEntity::getOrgId);
		List<UserAssignmentEntity> assignments = userAssignmentMapper.selectList(query);
		Set<Long> orgIds = assignments.stream().map(UserAssignmentEntity::getOrgId).collect(Collectors.toSet());
		Map<Long, OrgEntity> orgById = new HashMap<>();
		if (!orgIds.isEmpty()) {
			for (OrgEntity org : orgMapper.selectByIds(orgIds)) orgById.put(org.getId(), org);
		}
		Map<Long, List<UserAssignmentVO>> result = new HashMap<>();
		for (UserAssignmentEntity assignment : assignments) {
			OrgEntity org = orgById.get(assignment.getOrgId());
			if (org == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "用户任职关联了无效组织");
			UserAssignmentVO vo = new UserAssignmentVO();
			vo.setId(assignment.getId());
			vo.setOrgId(assignment.getOrgId());
			vo.setOrgName(org.getName());
			vo.setOrgNamePath(org.getNamePath());
			vo.setPosition(assignment.getPosition());
			vo.setIsOrgLeader(assignment.getIsOrgLeader());
			vo.setIsPrimary(assignment.getIsPrimary());
			result.computeIfAbsent(assignment.getUserId(), ignored -> new ArrayList<>()).add(vo);
		}
		for (List<UserAssignmentVO> userAssignments : result.values()) {
			userAssignments.sort(Comparator
					.comparing((UserAssignmentVO assignment) -> !Boolean.TRUE.equals(assignment.getIsPrimary()))
					.thenComparing(UserAssignmentVO::getOrgNamePath));
		}
		return result;
	}

	public AttachmentEntity requireAvatar(Long userId) {
		UserEntity user = mapper.selectById(userId);
		if (user == null || user.getAvatarAttachmentId() == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "用户头像未设置");
		}
		return attachmentService.requireAggregateAttachment(user.getAvatarAttachmentId(),
				UserResourceRegistration.RESOURCE_TYPE, String.valueOf(userId));
	}

	private String avatarUrl(Long userId, Long attachmentId) {
		return attachmentId == null ? null : "/sys/base/user/avatar/" + userId + "?v=" + attachmentId;
	}

	private void promoteAvatar(UserSaveForm form, Long userId) {
		if (form.getAvatarAttachmentId() == null) return;
		AttachmentPromoteForm promoteForm = new AttachmentPromoteForm();
		promoteForm.setAttachmentIds(List.of(form.getAvatarAttachmentId()));
		promoteForm.setBizType(UserResourceRegistration.RESOURCE_TYPE);
		promoteForm.setBizId(String.valueOf(userId));
		promoteForm.setUploadSessions(form.getAttachmentUploadSessions());
		try {
			attachmentService.promoteForAggregate(promoteForm);
		} catch (IOException exception) {
			throw new BizException(ResultEnum.CONFIG_ERROR, "用户头像确认失败: " + exception.getMessage());
		}
	}

	private Long findTemporaryAvatarId(Long attachmentId) {
		if (attachmentId == null) return null;
		return attachmentService.listByIds(List.of(attachmentId)).stream()
				.filter(attachment -> Boolean.TRUE.equals(attachment.getIsTemp()))
				.map(AttachmentVO::getId).findFirst().orElse(null);
	}

	private void deleteReplacedAvatar(UserEntity previous, Long nextAvatarId) {
		if (previous == null || previous.getAvatarAttachmentId() == null
				|| previous.getAvatarAttachmentId().equals(nextAvatarId)) return;
		deleteAvatarForCompensation(previous.getAvatarAttachmentId());
	}

	private void deleteAvatarForCompensation(Long attachmentId) {
		if (attachmentId == null) return;
		try {
			attachmentService.deleteForAggregate(attachmentId);
		} catch (IOException | RuntimeException exception) {
			log.warn("用户头像清理失败，需按附件ID重试: id={}", attachmentId, exception);
		}
	}

	@BizLog(value = "重置用户密码", recordResponse = false)
	public ResetPasswordVO resetPassword(Long userId) {
		String password = txService.resetPassword(userId);
		authorizationStateHelper.terminateUsers(List.of(userId), SessionTerminationReason.PASSWORD_RESET_TERMINATED);
		return new ResetPasswordVO(password);
	}

	public void changeResetPassword(Long userId, String newPassword) {
		if (newPassword == null || newPassword.isBlank()) {
			throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能为空");
		}
		txService.changeResetPassword(userId, newPassword);
		authorizationStateHelper.terminateUsers(List.of(userId), SessionTerminationReason.PASSWORD_RESET_TERMINATED);
	}

	public UserInfoVO current() {
		// 直接走 mapper，避免自调用绕过缓存代理
		UserEntity userEntity = mapper.selectById(currentUserContext.getUserId());
		UserInfoVO vo = converter.toInfoVO(userEntity);
		vo.setAvatar(avatarUrl(userEntity.getId(), userEntity.getAvatarAttachmentId()));
		return vo;
	}

	@BizLog("修改个人主题")
	@CacheInvalidate(name = BaseCacheName.USER_INFO, key = "@currentUserContext.getUserId()")
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
	@Cached(cacheType = CacheType.REMOTE, name = BaseCacheName.USER_INFO,
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
		// 未分配组织的用户只能以禁用状态暂存。
		vo.setEnabled(false);
		// 可根据业务需要设置默认角色等
		return vo;
	}
}
