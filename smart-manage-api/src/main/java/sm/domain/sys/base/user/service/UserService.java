package sm.domain.sys.base.user.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.UserConstant;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.domain.sys.base.login.model.vo.LoginVO;
import sm.domain.sys.base.permission.service.PermissionService;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.model.form.UserListForm;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.form.UserRoleAssignmentSaveForm;
import sm.domain.sys.base.user.model.form.CurrentUserPasswordForm;
import sm.domain.sys.base.user.model.form.CurrentUserProfileForm;
import sm.domain.sys.base.user.model.form.CurrentUserContactForm;
import sm.domain.sys.base.user.model.vo.UserCreateNewDataVO;
import sm.domain.sys.base.user.model.vo.UserInfoVO;
import sm.domain.sys.base.user.model.vo.UserDetailVO;
import sm.domain.sys.base.user.model.vo.UserListVO;
import sm.domain.sys.base.user.model.vo.ResetPasswordVO;
import sm.domain.sys.base.user.model.vo.UserAuthentication;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;
import sm.domain.sys.base.attachment.contract.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.contract.model.vo.AttachmentVO;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserRoleMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.org.model.OrgType;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.vo.UserAssignmentVO;
import sm.domain.sys.base.user.model.vo.UserRoleAssignmentWorkspaceVO;
import sm.domain.sys.base.user.model.vo.UserRoleOrganizationVO;
import sm.domain.sys.base.user.model.vo.UserAssignedRoleVO;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.system.helper.Argon2Helper;
import sm.system.auth.SessionTerminationReason;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListSqlQuery;
import sm.system.security.CsrfTokenManager;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.io.IOException;
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
	private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
			"name", ListSqlQuery.string("a.name", true),
			"number", ListSqlQuery.string("a.number", true),
			"username", ListSqlQuery.string("a.username", true),
			"enabled", ListSqlQuery.bool("a.enabled", true));
	private final UserMapper mapper;
	private final UserRoleMapper userRoleMapper;
	private final UserAssignmentMapper userAssignmentMapper;
	private final OrgMapper orgMapper;
	private final AttachmentService attachmentService;
	private final UserTxService txService;
	private final PermissionService permissionService;
	private final AuthorizationStateHelper authorizationStateHelper;
	private final UserCacheAccessor userCacheAccessor;
	private final UserConverter converter;
	private final CurrentUserContext currentUserContext;
	private final CsrfTokenManager csrfTokenManager;

	public PageData<UserListVO> listPage(UserListForm form) {
		List<Long> scopedOrgIds = resolveScopedOrgIds(form);
		if (scopedOrgIds != null && scopedOrgIds.isEmpty()) {
			return PageData.of(0, form.getPageNum(), form.getPageSize(), List.of());
		}
		Page<UserEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		String keyword = form.getKeyword() == null ? null : form.getKeyword().trim();
		Page<UserEntity> result = mapper.selectScopedPage(page, keyword, scopedOrgIds,
				Boolean.TRUE.equals(form.getUnassigned()), ListSqlQuery.of(form, LIST_FIELDS));
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
		List<Long> previousAuthorizationOrgIds = previous == null
				? List.of()
				: userRoleMapper.selectOrgIdsByUserId(userId);
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
		// 任职移除会同步删除角色，必须刷新删除前仍存在的精确组织授权缓存。
		for (Long orgId : previousAuthorizationOrgIds) {
			authorizationStateHelper.refreshUserAuthorization(savedId, orgId);
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

	/** 一次加载用户摘要、全部任职组织和各组织的精确角色关系。 */
	public UserRoleAssignmentWorkspaceVO roleAssignmentWorkspace(Long userId) {
		UserEntity user = mapper.selectById(userId);
		if (user == null) throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
		List<UserAssignmentVO> assignments = loadAssignments(List.of(userId), null)
				.getOrDefault(userId, List.of());
		Map<Long, List<UserAssignedRoleVO>> rolesByOrgId = mapper.selectAssignedRoles(userId)
				.stream()
				.collect(Collectors.groupingBy(UserAssignedRoleVO::getOrgId));

		List<UserRoleOrganizationVO> organizations = new ArrayList<>();
		for (UserAssignmentVO assignment : assignments) {
			UserRoleOrganizationVO organization = new UserRoleOrganizationVO();
			organization.setOrg(assignment.getOrg());
			organization.setOrgNamePath(assignment.getOrgNamePath());
			organization.setPosition(assignment.getPosition());
			organization.setIsPrimary(assignment.getIsPrimary());
			organization.setRoles(rolesByOrgId.getOrDefault(assignment.getOrg().getId(), List.of()));
			organizations.add(organization);
		}
		UserRoleAssignmentWorkspaceVO workspace = new UserRoleAssignmentWorkspaceVO();
		workspace.setId(user.getId());
		workspace.setName(user.getName());
		workspace.setUsername(user.getUsername());
		workspace.setNumber(user.getNumber());
		workspace.setOrganizations(organizations);
		return workspace;
	}

	/** 整体保存用户全部任职组织的角色结果，并精确刷新变更前后的授权缓存。 */
	@BizLog("分配用户角色")
	public void saveRoleAssignment(UserRoleAssignmentSaveForm form) {
		LinkedHashSet<Long> affectedOrgIds = new LinkedHashSet<>(
				userRoleMapper.selectOrgIdsByUserId(form.getUserId()));
		for (var assignment : form.getAssignments()) affectedOrgIds.add(assignment.getOrgId());
		txService.saveRoleAssignment(form);
		for (Long orgId : affectedOrgIds) {
			authorizationStateHelper.refreshUserAuthorization(form.getUserId(), orgId);
		}
	}

	/** 查询用户基础详情和全部任职；角色关系必须通过带组织上下文的独立查询获取。 */
	public UserDetailVO detail(Long id) {
		UserEntity userEntity = mapper.selectById(id);
		if (userEntity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "用户不存在");
		}
		UserDetailVO userInfoVO = converter.toDetailVO(userEntity);
		userInfoVO.setAvatar(avatarUrl(id, userEntity.getAvatarAttachmentId()));
		userInfoVO.setAssignments(loadAssignments(List.of(id), null).getOrDefault(id, List.of()));
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

	/** 二级认证只验证当前真实管理员自己的密码。 */
	public boolean verifyAdministratorPassword(Long userId, String password) {
		UserEntity user = mapper.selectById(userId);
		return user != null
				&& UserConstant.SUPER_ADMIN.equals(user.getUsername())
				&& Boolean.TRUE.equals(user.getEnabled())
				&& Argon2Helper.verify(user.getPassword(), password);
	}

	/** 代登录不验证目标密码，但复用正式登录的账号和主职组织有效性校验。 */
	public UserAuthentication authenticateTemporaryLogin(Long userId, String expectedUsername) {
		UserEntity user = mapper.selectById(userId);
		if (user == null || !user.getUsername().equals(expectedUsername)
				|| UserConstant.SUPER_ADMIN.equals(user.getUsername())) {
			return UserAuthentication.failed("用户名或密码错误");
		}
		if (!Boolean.TRUE.equals(user.getEnabled())) {
			return UserAuthentication.failed("用户名或密码错误");
		}
		UserAssignmentEntity primaryAssignment = userAssignmentMapper.selectOne(
				new LambdaQueryWrapper<UserAssignmentEntity>()
						.eq(UserAssignmentEntity::getUserId, user.getId())
						.eq(UserAssignmentEntity::getIsPrimary, true));
		if (primaryAssignment == null) {
			return UserAuthentication.failed("用户名或密码错误");
		}
		OrgEntity primaryOrganization = orgMapper.selectById(primaryAssignment.getOrgId());
		if (primaryOrganization == null || !Boolean.TRUE.equals(primaryOrganization.getEnabled())
				|| Boolean.TRUE.equals(primaryOrganization.getArchived())) {
			return UserAuthentication.failed("用户名或密码错误");
		}
		return new UserAuthentication(user.getId(), user.getUsername(), user.getName(), false,
				false, primaryOrganization.getId(), null);
	}

	/** 凭据验证且无需强制改密后，才创建正式登录状态。 */
	public LoginVO completeLogin(UserAuthentication authentication) {
		StpUtil.login(authentication.userId());
		currentUserContext.initializeIdentity(
				authentication.orgId(), authentication.username(), authentication.administrator());
		csrfTokenManager.initializeCurrentSession();

		LoginVO vo = new LoginVO();
		vo.setAuthenticated(true);
		return vo;
	}

	/** 创建与管理员当前令牌完全独立、固定三十分钟有效的代登录会话。 */
	public LoginVO completeTemporaryLogin(UserAuthentication authentication, Long issuerUserId,
			String grantId, String reason) {
		SaLoginParameter parameter = new SaLoginParameter()
				.setTimeout(30 * 60)
				.setDevice("temporary-admin-login")
				.setIsLastingCookie(false);
		StpUtil.login(authentication.userId(), parameter);
		currentUserContext.initializeIdentity(authentication.orgId(), authentication.username(), false);
		csrfTokenManager.initializeCurrentSession();
		var tokenSession = StpUtil.getTokenSession();
		tokenSession.set("authenticationMethod", "TEMPORARY_ADMIN_GRANT");
		tokenSession.set("issuerUserId", issuerUserId);
		tokenSession.set("grantId", grantId);
		tokenSession.set("temporaryLoginReason", reason);

		LoginVO vo = new LoginVO();
		vo.setAuthenticated(true);
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
			vo.setOrg(new ReferenceVO(org.getId(), org.getNumber(), org.getName()));
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
		promoteAvatar(form.getAvatarAttachmentId(), form.getAttachmentUploadSessions(), userId);
	}

	private void promoteAvatar(Long avatarAttachmentId, Map<Long, String> uploadSessions, Long userId) {
		if (avatarAttachmentId == null || !uploadSessions.containsKey(avatarAttachmentId)) return;
		AttachmentPromoteForm promoteForm = new AttachmentPromoteForm();
		promoteForm.setAttachmentIds(List.of(avatarAttachmentId));
		promoteForm.setBizType(UserResourceRegistration.RESOURCE_TYPE);
		promoteForm.setBizId(String.valueOf(userId));
		promoteForm.setUploadSessions(uploadSessions);
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
		assembleCurrentOrganization(vo, userEntity.getId());
		return vo;
	}

	/**
	 * 切换会话组织前必须重新校验有效任职，不能信任前端传入的组织范围。
	 */
	public void switchCurrentOrganization(Long orgId) {
		Long userId = currentUserContext.getUserId();
		UserAssignmentEntity assignment = userAssignmentMapper.selectOne(
				new LambdaQueryWrapper<UserAssignmentEntity>()
						.eq(UserAssignmentEntity::getUserId, userId)
						.eq(UserAssignmentEntity::getOrgId, orgId));
		if (assignment == null) {
			throw new BizException(ResultEnum.PERMISSION_ERROR, "只能切换到自己的任职组织");
		}
		OrgEntity organization = orgMapper.selectById(orgId);
		if (organization == null || !Boolean.TRUE.equals(organization.getEnabled())
				|| Boolean.TRUE.equals(organization.getArchived())) {
			throw new BizException(ResultEnum.PARAM_ERROR, "目标组织不可用");
		}
		currentUserContext.setOrgId(orgId);
	}

	@BizLog("修改个人资料")
	@CacheInvalidate(name = BaseCacheName.USER_INFO, key = "@currentUserContext.getUserId()")
	public void updateCurrentProfile(CurrentUserProfileForm form) {
		Long userId = currentUserContext.getUserId();
		UserEntity previous = requireUser(userId);
		Long temporaryAvatarId = findTemporaryAvatarId(form.getAvatarAttachmentId());
		promoteAvatar(form.getAvatarAttachmentId(), form.getAttachmentUploadSessions(), userId);
		try {
			txService.updateCurrentProfile(userId, form.getName(), form.getGender(), form.getBirthday(),
					form.getAvatarAttachmentId());
			deleteReplacedAvatar(previous, form.getAvatarAttachmentId());
		} catch (RuntimeException exception) {
			deleteAvatarForCompensation(temporaryAvatarId);
			throw exception;
		}
	}

	@BizLog(value = "修改个人联系方式", recordResponse = false)
	@CacheInvalidate(name = BaseCacheName.USER_INFO, key = "@currentUserContext.getUserId()")
	public void updateCurrentContact(CurrentUserContactForm form) {
		String password;
		try {
			password = sm.system.helper.SM2Helper.decryptJsCiphertext(form.getPassword());
		} catch (RuntimeException exception) {
			throw new BizException(ResultEnum.PARAM_ERROR, "密码加密数据无效");
		}
		txService.updateCurrentContact(currentUserContext.getUserId(), password, form.getType(), form.getValue());
	}

	@BizLog(value = "修改个人密码", recordResponse = false)
	public void updateCurrentPassword(CurrentUserPasswordForm form) {
		String currentPassword;
		String newPassword;
		try {
			currentPassword = sm.system.helper.SM2Helper.decryptJsCiphertext(form.getCurrentPassword());
			newPassword = sm.system.helper.SM2Helper.decryptJsCiphertext(form.getNewPassword());
		} catch (RuntimeException exception) {
			throw new BizException(ResultEnum.PARAM_ERROR, "密码加密数据无效");
		}
		if (newPassword.length() < 8) {
			throw new BizException(ResultEnum.PARAM_ERROR, "新密码不能少于8位");
		}
		Long userId = currentUserContext.getUserId();
		txService.updateCurrentPassword(userId, currentPassword, newPassword);
		authorizationStateHelper.terminateUsers(List.of(userId), SessionTerminationReason.PASSWORD_RESET_TERMINATED);
	}

	private void assembleCurrentOrganization(UserInfoVO vo, Long userId) {
		List<UserAssignmentEntity> assignments = userAssignmentMapper.selectList(
				new LambdaQueryWrapper<UserAssignmentEntity>()
						.eq(UserAssignmentEntity::getUserId, userId)
						.orderByDesc(UserAssignmentEntity::getIsPrimary)
						.orderByAsc(UserAssignmentEntity::getOrgId));
		Set<Long> organizationIds = assignments.stream()
				.map(UserAssignmentEntity::getOrgId)
				.collect(Collectors.toSet());
		Map<Long, OrgEntity> organizationById = new HashMap<>();
		if (!organizationIds.isEmpty()) {
			for (OrgEntity organization : orgMapper.selectByIds(organizationIds)) {
				organizationById.put(organization.getId(), organization);
			}
		}
		List<UserAssignmentVO> availableAssignments = new ArrayList<>();
		Map<Long, OrgEntity> availableOrganizations = new HashMap<>();
		for (UserAssignmentEntity assignment : assignments) {
			OrgEntity organization = organizationById.get(assignment.getOrgId());
			if (organization == null || !Boolean.TRUE.equals(organization.getEnabled())
					|| Boolean.TRUE.equals(organization.getArchived())) {
				continue;
			}
			availableOrganizations.put(organization.getId(), organization);
			UserAssignmentVO assignmentVO = new UserAssignmentVO();
			assignmentVO.setId(assignment.getId());
			assignmentVO.setOrg(new ReferenceVO(
					organization.getId(), organization.getNumber(), organization.getName()));
			assignmentVO.setOrgNamePath(organization.getNamePath());
			assignmentVO.setPosition(assignment.getPosition());
			assignmentVO.setIsOrgLeader(assignment.getIsOrgLeader());
			assignmentVO.setIsPrimary(assignment.getIsPrimary());
			availableAssignments.add(assignmentVO);
		}
		Long currentOrgId = currentUserContext.getOrgId();
		OrgEntity currentOrganization = availableOrganizations.get(currentOrgId);
		if (currentOrganization == null) {
			throw new BizException(ResultEnum.PERMISSION_ERROR, "当前组织不在用户的有效任职范围内");
		}
		vo.setAssignments(availableAssignments);
		vo.setCurrentOrgId(currentOrgId);
		vo.setCurrentOrgName(currentOrganization.getName());
		vo.setCompanyName(resolveCompanyName(currentOrganization));
	}

	private String resolveCompanyName(OrgEntity organization) {
		OrgEntity current = organization;
		String highestOrganizationName = organization.getName();
		while (current != null) {
			highestOrganizationName = current.getName();
			if (OrgType.COMPANY.equals(current.getOrgType())) return current.getName();
			current = current.getParentId() == null ? null : orgMapper.selectById(current.getParentId());
		}
		return highestOrganizationName;
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

	/** Controller 需要在返回安全配置前复核真实管理员身份。 */
	public void checkAdministrator() {
		currentUserContext.checkAdministrator();
	}

	/** Redis 远程缓存读取；仅供其他 Spring Bean 外部调用，确保缓存代理生效。 */
	public UserEntity requireUser(Long id) {
		return userCacheAccessor.requireUser(id);
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
