package sm.domain.sys.base.cloud.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.cloud.model.entity.CloudEntity;
import sm.domain.sys.base.cloud.model.form.CloudListForm;
import sm.domain.sys.base.cloud.model.form.CloudSelectForm;
import sm.domain.sys.base.cloud.model.form.CloudSaveForm;
import sm.domain.sys.base.cloud.model.vo.CloudCreateNewDataVO;
import sm.domain.sys.base.cloud.model.vo.CloudDetailVO;
import sm.domain.sys.base.cloud.model.vo.CloudListVO;
import sm.domain.sys.base.cloud.model.vo.CloudSelectVO;
import sm.domain.sys.base.cloud.mapper.CloudMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudService {
	private final CloudMapper mapper;
	private final CloudTxService txService;
	private final CloudConverter converter;

	public PageData<CloudListVO> listPage(CloudListForm form) {
		LambdaQueryWrapper<CloudEntity> qw = new LambdaQueryWrapper<CloudEntity>();
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String kw = "%" + form.getKeyword().trim() + "%";
			qw.and(condition -> condition.like(CloudEntity::getName, kw).or().like(CloudEntity::getNumber, kw));
		}
		if (form.getEnabled() != null) {
			qw.eq(CloudEntity::getEnabled, form.getEnabled());
		}
		qw.orderByAsc(CloudEntity::getSeq).orderByAsc(CloudEntity::getId);
		Page<CloudEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<CloudEntity> result = mapper.selectPage(page, qw);
		List<CloudListVO> vos = result.getRecords().stream().map(converter::toListVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

	public PageData<CloudSelectVO> select(CloudSelectForm form) {
		LambdaQueryWrapper<CloudEntity> qw = new LambdaQueryWrapper<CloudEntity>();
		if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
			String kw = "%" + form.getKeyword().trim() + "%";
			qw.and(condition -> condition.like(CloudEntity::getName, kw).or().like(CloudEntity::getNumber, kw));
		}
		if (form.getEnabled() != null) {
			qw.eq(CloudEntity::getEnabled, form.getEnabled());
		}
		qw.orderByAsc(CloudEntity::getSeq).orderByAsc(CloudEntity::getId);
		Page<CloudEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<CloudEntity> result = mapper.selectPage(page, qw);
		List<CloudSelectVO> vos = result.getRecords().stream().map(converter::toSelectVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
	}

	public CloudEntity getById(Long id) {
		return mapper.selectById(id);
	}

	public CloudDetailVO getDetail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "云ID不能为空");
		}
		CloudEntity entity = mapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "云不存在");
		}
		return converter.toDetailVO(entity);
	}

	public CloudCreateNewDataVO createNewData() {
		CloudCreateNewDataVO vo = new CloudCreateNewDataVO();
		vo.setSeq(99);
		vo.setEnabled(true);
		return vo;
	}

	@BizLog("保存云")
	public Long save(CloudSaveForm form) {
		return txService.save(form);
	}

	@BizLog("删除云")
	public void deleteById(Long id) {
		txService.deleteById(id);
	}

	@BizLog("启用云")
	public void enable(List<Long> ids) {
		txService.updateEnabled(ids, true);
	}

	@BizLog("禁用云")
	public void disable(List<Long> ids) {
		txService.updateEnabled(ids, false);
	}
}
