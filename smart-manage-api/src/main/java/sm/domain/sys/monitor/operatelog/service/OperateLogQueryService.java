package sm.domain.sys.monitor.operatelog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.monitor.operatelog.model.entity.OperateLogEntity;
import sm.domain.sys.monitor.operatelog.model.form.OperateLogListForm;
import sm.domain.sys.monitor.operatelog.model.vo.OperateLogDetailVO;
import sm.domain.sys.monitor.operatelog.model.vo.OperateLogListVO;
import sm.domain.sys.monitor.operatelog.mapper.OperateLogMapper;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;


@Service
@RequiredArgsConstructor
public class OperateLogQueryService {
	private final OperateLogMapper mapper;
	private final OperateLogConverter converter;

	public PageData<OperateLogListVO> listPage(OperateLogListForm form) {
		LambdaQueryWrapper<OperateLogEntity> qw = new LambdaQueryWrapper<OperateLogEntity>();
		if (StringUtils.hasText(form.getKeyword())) {
			String kw = "%" + form.getKeyword().trim() + "%";
			qw.and(condition -> condition.like(OperateLogEntity::getRequestUri, kw).or().like(OperateLogEntity::getMethodName, kw).or().like(OperateLogEntity::getBizName, kw));
		}
		if (form.getSuccess() != null) {
			qw.eq(OperateLogEntity::getSuccess, form.getSuccess());
		}
		if (form.getBeginTime() != null) {
			qw.ge(OperateLogEntity::getCreateTime, form.getBeginTime());
		}
		if (form.getEndTime() != null) {
			qw.le(OperateLogEntity::getCreateTime, form.getEndTime());
		}
		qw.orderByDesc(OperateLogEntity::getCreateTime);
		Page<OperateLogEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<OperateLogEntity> result = mapper.selectPage(page, qw);
		var records = result.getRecords().stream().map(converter::toListVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
	}

	public OperateLogDetailVO getById(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "操作日志ID不能为空");
		}
		OperateLogEntity entity = mapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "操作日志不存在");
		}
		return converter.toDetailVO(entity);
	}
}
