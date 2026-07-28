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

/**
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionService {
	private final PermissionMapper mapper;
	private final PermissionTxService txService;
	private final PermissionConverter converter;

	public PageData<PermissionListVO> listPage(PermissionListForm form) {
		LambdaQueryWrapper<PermissionEntity> qw = new LambdaQueryWrapper<PermissionEntity>();
		qw.eq(form.getAppId() != null, PermissionEntity::getAppId, form.getAppId());
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String keyword = form.getKeyword().trim();
			qw.and(condition -> condition.like(PermissionEntity::getName, keyword)
					.or().like(PermissionEntity::getNumber, keyword));
		}
		qw.orderByAsc(PermissionEntity::getNumber);
		Page<PermissionEntity> result = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), qw);
		List<PermissionListVO> records = result.getRecords().stream().map(converter::toListVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
	}

	/**
	 * 角色权限分配需要一次性展示全部权限，仅返回选择和分组所需的轻量字段。
	 */
	public List<PermissionSelectVO> listAll() {
		return mapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
				.orderByAsc(PermissionEntity::getNumber)
				.orderByAsc(PermissionEntity::getId))
				.stream()
				.map(converter::toSelectVO)
				.toList();
	}

	/**
	 * 基础资料选择：分页查询权限。
	 * 支持按应用、关键词过滤；按编码排序。
	 */
	public PageData<PermissionSelectVO> select(PermissionSelectForm form) {
		LambdaQueryWrapper<PermissionEntity> qw = new LambdaQueryWrapper<PermissionEntity>();
		qw.eq(form.getAppId() != null, PermissionEntity::getAppId, form.getAppId());
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String keyword = form.getKeyword().trim();
			qw.and(condition -> condition.like(PermissionEntity::getNumber, keyword)
					.or().like(PermissionEntity::getName, keyword));
		}
		qw.orderByAsc(PermissionEntity::getNumber);
		Page<PermissionEntity> result = mapper.selectPage(new Page<>(form.getPageNum(), form.getPageSize()), qw);
		List<PermissionSelectVO> records = result.getRecords().stream().map(converter::toSelectVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
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

	public PermissionEntity getById(Long id) {
		return mapper.selectById(id);
	}

	public PermissionDetailVO getDetail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "权限ID不能为空");
		}
		PermissionEntity entity = mapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "权限不存在");
		}
		return converter.toDetailVO(entity);
	}

	public PermissionCreateNewDataVO createNewData() {
		return new PermissionCreateNewDataVO();
	}

	@BizLog("保存权限")
	public Long save(PermissionSaveForm form) {
		return txService.save(form);
	}

	@BizLog("删除权限")
	public void deleteById(Long id) {
		txService.deleteById(id);
	}
}
