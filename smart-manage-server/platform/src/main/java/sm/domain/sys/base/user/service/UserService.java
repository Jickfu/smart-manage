package sm.domain.sys.base.user.service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.BaseCacheName;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.common.helper.UserCacheInvalidator;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.user.converter.UserConverter;
import sm.domain.sys.base.user.model.form.UserListForm;
import sm.domain.sys.base.user.model.form.UserSaveForm;
import sm.domain.sys.base.user.model.vo.UserCreateNewDataVO;
import sm.domain.sys.base.user.model.vo.UserDetailVO;
import sm.domain.sys.base.user.model.vo.UserListVO;
import sm.domain.sys.base.attachment.contract.AttachmentPromoteCommand;
import sm.domain.sys.base.attachment.contract.AttachmentReference;
import sm.domain.sys.base.attachment.service.AttachmentService;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.mapper.UserAssignmentMapper;
import sm.domain.sys.base.org.contract.OrgReference;
import sm.domain.sys.base.org.contract.OrgReferenceReader;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;
import sm.domain.sys.base.user.model.vo.UserAssignmentVO;
import sm.domain.sys.base.common.model.vo.ReferenceVO;
import sm.system.aop.log.BizLog;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListSqlQuery;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 用户基础资料管理服务：负责 CRUD、启停状态和管理员维护流程。
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
	private final UserAssignmentMapper userAssignmentMapper;
	private final OrgReferenceReader orgReferenceReader;
	private final AttachmentService attachmentService;
	private final UserTxService txService;
	private final UserCacheInvalidator userCacheInvalidator;
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
				Boolean.TRUE.equals(form.getUnassigned()), null, ListSqlQuery.of(form, LIST_FIELDS));
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
			deleteReplacedAvatar(
					previous == null ? null : previous.getAvatarAttachmentId(), form.getAvatarAttachmentId());
		} catch (RuntimeException exception) {
			deleteAvatarForCompensation(temporaryAvatarId);
			throw exception;
		}
		if (form.getAssignments().isEmpty()) {
			userCacheInvalidator.tryRefreshUsers(List.of(savedId));
		}
		return savedId;
	}

	@BizLog("删除用户")
	@CacheInvalidate(name = BaseCacheName.USER_INFO, key = "#id")
	public void deleteById(Long id) {
		UserEntity user = mapper.selectById(id);
		txService.deleteById(id);
		if (user != null) deleteAvatarForCompensation(user.getAvatarAttachmentId());
		userCacheInvalidator.tryRefreshUsers(List.of(id));
	}

	@BizLog("启用用户")
	public void enable(List<Long> ids) {
		txService.updateEnabled(ids, true);
		userCacheInvalidator.tryRefreshUsers(ids);
	}

	@BizLog("禁用用户")
	public void disable(List<Long> ids) {
		txService.updateEnabled(ids, false);
		userCacheInvalidator.tryRefreshUsers(ids);
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

	private List<Long> resolveScopedOrgIds(UserListForm form) {
		if (Boolean.TRUE.equals(form.getUnassigned()) || form.getOrgId() == null) return null;
		OrgReference selected = orgReferenceReader.require(form.getOrgId());
		if (!Boolean.TRUE.equals(form.getIncludeDescendants())) return List.of(selected.id());
		String prefix = selected.numberPath() + "/";
		return orgReferenceReader.findAll().stream()
				.filter(org -> org.id().equals(selected.id()) || (org.numberPath() != null && org.numberPath().startsWith(prefix)))
				.map(OrgReference::id).toList();
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
		Map<Long, OrgReference> orgById = orgReferenceReader.findByIds(orgIds);
		Map<Long, List<UserAssignmentVO>> result = new HashMap<>();
		for (UserAssignmentEntity assignment : assignments) {
			OrgReference org = orgById.get(assignment.getOrgId());
			if (org == null) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "用户任职关联了无效组织");
			UserAssignmentVO vo = new UserAssignmentVO();
			vo.setId(assignment.getId());
			vo.setOrg(new ReferenceVO(org.id(), org.number(), org.name()));
			vo.setOrgNamePath(org.namePath());
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

	private String avatarUrl(Long userId, Long attachmentId) {
		return attachmentId == null ? null : "/sys/base/user/avatar/" + userId + "?v=" + attachmentId;
	}

	private void promoteAvatar(UserSaveForm form, Long userId) {
		promoteAvatar(form.getAvatarAttachmentId(), form.getAttachmentUploadSessions(), userId);
	}

	private void promoteAvatar(Long avatarAttachmentId, Map<Long, String> uploadSessions, Long userId) {
		if (avatarAttachmentId == null || !uploadSessions.containsKey(avatarAttachmentId)) return;
		AttachmentPromoteCommand promoteCommand = new AttachmentPromoteCommand();
		promoteCommand.setAttachmentIds(List.of(avatarAttachmentId));
		promoteCommand.setBizType(UserResourceRegistration.RESOURCE_TYPE);
		promoteCommand.setBizId(String.valueOf(userId));
		promoteCommand.setUploadSessions(uploadSessions);
		try {
			attachmentService.promoteForAggregate(promoteCommand);
		} catch (IOException exception) {
			throw new BizException(ResultEnum.CONFIG_ERROR, "用户头像确认失败: " + exception.getMessage());
		}
	}

	private Long findTemporaryAvatarId(Long attachmentId) {
		if (attachmentId == null) return null;
		return attachmentService.listByIds(List.of(attachmentId)).stream()
				.filter(attachment -> Boolean.TRUE.equals(attachment.getIsTemp()))
				.map(AttachmentReference::getId).findFirst().orElse(null);
	}

	private void deleteReplacedAvatar(Long previousAvatarAttachmentId, Long nextAvatarId) {
		if (previousAvatarAttachmentId == null || previousAvatarAttachmentId.equals(nextAvatarId)) return;
		deleteAvatarForCompensation(previousAvatarAttachmentId);
	}

	private void deleteAvatarForCompensation(Long attachmentId) {
		if (attachmentId == null) return;
		try {
			attachmentService.deleteForAggregate(attachmentId);
		} catch (IOException | RuntimeException exception) {
			log.warn("用户头像清理失败，需按附件ID重试: id={}", attachmentId, exception);
		}
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
