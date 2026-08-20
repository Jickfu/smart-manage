package sm.domain.sys.base.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.domain.sys.base.permission.model.form.PermissionListForm;
import sm.domain.sys.base.permission.model.form.PermissionSaveForm;
import sm.domain.sys.base.permission.model.form.PermissionSelectForm;
import sm.domain.sys.base.permission.model.vo.PermissionCreateNewDataVO;
import sm.domain.sys.base.permission.model.vo.PermissionDetailVO;
import sm.domain.sys.base.permission.model.vo.PermissionListVO;
import sm.domain.sys.base.permission.model.vo.PermissionSelectVO;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.List;
import java.util.Map;
import sm.system.query.ListSqlQuery;
import sm.domain.sys.base.common.helper.AuthorizationStateHelper;

/**
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionService {
	private static final Map<String, ListSqlQuery.Field> LIST_FIELDS = Map.of(
			"number", ListSqlQuery.string("a.number", true),
			"name", ListSqlQuery.string("a.name", true),
			"featureName", ListSqlQuery.string("COALESCE(b.custom_name, b.default_name)", false),
			"appName", ListSqlQuery.string("c.name", false));
	private final PermissionMapper mapper;
	private final PermissionTxService txService;
	private final AuthorizationStateHelper authorizationStateHelper;

	public PageData<PermissionListVO> listPage(PermissionListForm form) {
		Page<PermissionListVO> result = mapper.selectListPage(
				new Page<>(form.getPageNum(), form.getPageSize()), form, ListSqlQuery.of(form, LIST_FIELDS));
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), result.getRecords());
	}

	/**
	 * 角色权限分配需要一次性展示全部权限，仅返回选择和分组所需的轻量字段。
	 */
	public List<PermissionSelectVO> listAll() {
		return mapper.selectAll();
	}

	/**
	 * 基础资料选择：分页查询权限。
	 * 支持按应用、关键词过滤；按编码排序。
	 */
	public PageData<PermissionSelectVO> select(PermissionSelectForm form) {
		Page<PermissionSelectVO> result = mapper.selectPage(
				new Page<>(form.getPageNum(), form.getPageSize()), form);
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), result.getRecords());
	}

	/**
	 * 当前用户在指定组织下拥有的权限编码
	 */
	public List<String> getUserPermissions(Long userId, Long orgId) {
		if (userId == null || orgId == null) {
			return List.of();
		}
		return mapper.selectUserPermissionNumbers(userId, orgId, null);
	}

	/**
	 * 当前用户在指定组织下按前缀过滤的权限编码
	 */
	public List<String> getUserPermissionsByPrefix(Long userId, Long orgId, String prefix) {
		if (userId == null || orgId == null) {
			return List.of();
		}
		return mapper.selectUserPermissionNumbers(userId, orgId, prefix);
	}

	public PermissionDetailVO detail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "权限ID不能为空");
		}
		PermissionDetailVO detail = mapper.selectDetailById(id);
		if (detail == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "权限不存在");
		}
		return detail;
	}

	public PermissionCreateNewDataVO createNewData() {
		return new PermissionCreateNewDataVO();
	}

	@BizLog("保存权限")
	public Long save(PermissionSaveForm form) {
		var affectedUsers = form.getId() == null ? List.<sm.domain.sys.base.user.model.entity.UserRoleEntity>of()
				: authorizationStateHelper.permissionUserScopes(form.getId());
		Long permissionId = txService.save(form);
		authorizationStateHelper.refreshScopes(affectedUsers);
		return permissionId;
	}

	@BizLog("删除权限")
	public void deleteById(Long id) {
		var affectedUsers = authorizationStateHelper.permissionUserScopes(id);
		txService.deleteById(id);
		authorizationStateHelper.refreshScopes(affectedUsers);
	}
}
