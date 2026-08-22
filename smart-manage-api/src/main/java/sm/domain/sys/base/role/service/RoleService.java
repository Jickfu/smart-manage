package sm.domain.sys.base.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.domain.sys.base.role.model.form.RoleListForm;
import sm.domain.sys.base.role.model.form.RoleSaveForm;
import sm.domain.sys.base.role.model.form.RolePermissionAssignForm;
import sm.domain.sys.base.role.model.form.RoleSelectForm;
import sm.domain.sys.base.role.model.vo.RoleCreateNewDataVO;
import sm.domain.sys.base.role.model.vo.RoleDetailVO;
import sm.domain.sys.base.role.model.vo.RoleListVO;
import sm.domain.sys.base.role.model.vo.RoleSelectVO;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.role.model.entity.RolePermissionEntity;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;
import sm.system.query.ListQueryUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleService {
	private static final Map<String, ListQueryUtil.Field<RoleEntity>> LIST_FIELDS = Map.of(
			"number", ListQueryUtil.string(RoleEntity::getNumber, true),
			"name", ListQueryUtil.string(RoleEntity::getName, true),
			"description", ListQueryUtil.string(RoleEntity::getDescription, false));
	private final RoleMapper mapper;
	private final RolePermissionMapper permissionMapper;
	private final RoleTxService txService;
	private final AuthorizationStateHelper authorizationStateHelper;
	private final RoleConverter converter;

	public PageData<RoleListVO> listPage(RoleListForm form) {
		LambdaQueryWrapper<RoleEntity> qw = new LambdaQueryWrapper<RoleEntity>();
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String kw = form.getKeyword().trim();
			qw.and(condition -> condition.like(RoleEntity::getName, kw).or().like(RoleEntity::getNumber, kw));
		}
		ListQueryUtil.apply(qw, form, LIST_FIELDS);
		if (!ListQueryUtil.hasSort(form)) qw.orderByAsc(RoleEntity::getId);
		else if (!ListQueryUtil.isSortedBy(form, "id")) qw.orderByAsc(RoleEntity::getId);
		Page<RoleEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<RoleEntity> result = mapper.selectPage(page, qw);
		var vos = result.getRecords().stream().map(converter::toListVO).collect(Collectors.toList());
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

	/**
	 * 基础资料选择：分页查询角色。
	 */
	public PageData<RoleSelectVO> select(RoleSelectForm form) {
		LambdaQueryWrapper<RoleEntity> qw = new LambdaQueryWrapper<RoleEntity>();
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String kw = form.getKeyword().trim();
			qw.and(condition -> condition.like(RoleEntity::getNumber, kw)
					.or().like(RoleEntity::getName, kw)
					.or().like(RoleEntity::getDescription, kw));
		}
		if (form.getExcludedIds() != null && !form.getExcludedIds().isEmpty()) {
			qw.notIn(RoleEntity::getId, form.getExcludedIds());
		}
		qw.orderByAsc(RoleEntity::getNumber).orderByAsc(RoleEntity::getId);
		Page<RoleEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<RoleEntity> result = mapper.selectPage(page, qw);
		List<RoleSelectVO> voList = result.getRecords().stream().map(converter::toSelectVO).collect(Collectors.toList());
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), voList);
	}

	public RoleDetailVO detail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "角色ID不能为空");
		}
		RoleEntity entity = mapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "角色不存在");
		}
		return assembleDetailVO(entity);
	}

	/** 详情除纯字段外还需要查询权限关系，因此保持显式业务组装。 */
	private RoleDetailVO assembleDetailVO(RoleEntity entity) {
		RoleDetailVO vo = converter.toDetailVO(entity);
		vo.setPermissionIds(permissionMapper.selectList(new LambdaQueryWrapper<RolePermissionEntity>()
					.select(RolePermissionEntity::getPermissionId)
					.eq(RolePermissionEntity::getRoleId, entity.getId()))
				.stream()
				.map(RolePermissionEntity::getPermissionId)
				.toList());
		return vo;
	}

	public RoleCreateNewDataVO createNewData() {
		return new RoleCreateNewDataVO();
	}

	@BizLog("保存角色")
	public Long save(RoleSaveForm form) {
		Long roleId = txService.save(form);
		if (form.getId() != null) authorizationStateHelper.refreshRoleUsers(roleId);
		return roleId;
	}

	@BizLog("删除角色")
	public void deleteById(Long id) {
		var affectedUsers = authorizationStateHelper.roleUserScopes(id);
		txService.deleteById(id);
		authorizationStateHelper.refreshScopes(affectedUsers);
	}

	@BizLog("分配角色权限")
	public void assignPermissions(RolePermissionAssignForm form) {
		txService.assignPermissions(form);
		authorizationStateHelper.refreshRoleUsers(form.getRoleId());
	}

	/** 查询用户在当前组织下拥有的稳定角色编码，供 Sa-Token 角色能力使用。 */
	public List<String> getUserRoleNumbers(Long userId, Long orgId) {
		return mapper.selectUserRoleNumbers(userId, orgId);
	}
}
