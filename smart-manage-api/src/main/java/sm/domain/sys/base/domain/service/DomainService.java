package sm.domain.sys.base.domain.service;

import sm.domain.sys.base.domain.converter.DomainConverter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.domain.model.entity.DomainEntity;
import sm.domain.sys.base.domain.model.form.DomainListForm;
import sm.domain.sys.base.domain.model.form.DomainSelectForm;
import sm.domain.sys.base.domain.model.form.DomainSaveForm;
import sm.domain.sys.base.domain.model.vo.DomainCreateNewDataVO;
import sm.domain.sys.base.domain.model.vo.DomainDetailVO;
import sm.domain.sys.base.domain.model.vo.DomainListVO;
import sm.domain.sys.base.domain.model.vo.DomainSelectVO;
import sm.domain.sys.base.domain.mapper.DomainMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListQueryUtil;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DomainService {
	private static final Map<String, ListQueryUtil.Field<DomainEntity>> LIST_FIELDS = Map.of(
			"number", ListQueryUtil.string(DomainEntity::getNumber, true),
			"name", ListQueryUtil.string(DomainEntity::getName, true),
			"seq", ListQueryUtil.number(DomainEntity::getSeq, true),
			"enabled", ListQueryUtil.bool(DomainEntity::getEnabled, false),
			"createTime", ListQueryUtil.dateTime(DomainEntity::getCreateTime, true),
			"updateTime", ListQueryUtil.dateTime(DomainEntity::getUpdateTime, true));
	private final DomainMapper mapper;
	private final DomainTxService txService;
	private final DomainConverter converter;

	public PageData<DomainListVO> listPage(DomainListForm form) {
		LambdaQueryWrapper<DomainEntity> qw = new LambdaQueryWrapper<DomainEntity>();
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String kw = "%" + form.getKeyword().trim() + "%";
			qw.and(condition -> condition.like(DomainEntity::getName, kw).or().like(DomainEntity::getNumber, kw));
		}
		if (form.getEnabled() != null) {
			qw.eq(DomainEntity::getEnabled, form.getEnabled());
		}
		ListQueryUtil.apply(qw, form, LIST_FIELDS);
		if (!ListQueryUtil.hasSort(form)) qw.orderByAsc(DomainEntity::getSeq);
		if (!ListQueryUtil.isSortedBy(form, "id")) qw.orderByAsc(DomainEntity::getId);
		Page<DomainEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<DomainEntity> result = mapper.selectPage(page, qw);
		List<DomainListVO> vos = result.getRecords().stream().map(converter::toListVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

	public PageData<DomainSelectVO> select(DomainSelectForm form) {
		LambdaQueryWrapper<DomainEntity> qw = new LambdaQueryWrapper<DomainEntity>();
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String kw = "%" + form.getKeyword().trim() + "%";
			qw.and(condition -> condition.like(DomainEntity::getName, kw).or().like(DomainEntity::getNumber, kw));
		}
		if (form.getEnabled() != null) {
			qw.eq(DomainEntity::getEnabled, form.getEnabled());
		}
		qw.orderByAsc(DomainEntity::getSeq).orderByAsc(DomainEntity::getId);
		Page<DomainEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<DomainEntity> result = mapper.selectPage(page, qw);
		List<DomainSelectVO> vos = result.getRecords().stream().map(converter::toSelectVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

	public DomainDetailVO detail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "领域ID不能为空");
		}
		DomainEntity entity = mapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "领域不存在");
		}
		return converter.toDetailVO(entity);
	}

	public DomainCreateNewDataVO createNewData() {
		DomainCreateNewDataVO vo = new DomainCreateNewDataVO();
		vo.setSeq(99);
		vo.setEnabled(true);
		return vo;
	}

	@BizLog("保存领域")
	public Long save(DomainSaveForm form) {
		return txService.save(form);
	}

	@BizLog("删除领域")
	public void deleteById(Long id) {
		txService.deleteById(id);
	}

	@BizLog("启用领域")
	public void enable(List<Long> ids) {
		txService.updateEnabled(ids, true);
	}

	@BizLog("禁用领域")
	public void disable(List<Long> ids) {
		txService.updateEnabled(ids, false);
	}
}
