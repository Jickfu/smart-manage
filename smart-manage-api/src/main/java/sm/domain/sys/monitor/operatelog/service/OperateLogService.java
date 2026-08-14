package sm.domain.sys.monitor.operatelog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import sm.domain.sys.monitor.common.util.LogQueryValidator;
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
public class OperateLogService {
	private final OperateLogMapper mapper;
	private final OperateLogConverter converter;

	public PageData<OperateLogListVO> listPage(OperateLogListForm form) {
		return listPage(form, null, null);
	}

	/** 本人入口强制按服务端登录身份隔离，且只复用不含请求正文的列表投影。 */
	public PageData<OperateLogListVO> listCurrentPage(OperateLogListForm form, Long currentUserId) {
		if (currentUserId == null) {
			throw new BizException(ResultEnum.UNAUTHORIZED, "当前用户未登录");
		}
		return listPage(form, currentUserId, LocalDateTime.now().minusDays(7));
	}

	private PageData<OperateLogListVO> listPage(
			OperateLogListForm form, Long restrictedUserId, LocalDateTime restrictedBeginTime) {
		LogQueryValidator.validateTimeRange(form.getBeginTime(), form.getEndTime());
		LambdaQueryWrapper<OperateLogEntity> qw = new LambdaQueryWrapper<OperateLogEntity>();
		// 请求参数、响应正文和 User-Agent 只在详情读取。
		qw.select(OperateLogEntity::getId, OperateLogEntity::getBizName, OperateLogEntity::getSuccess,
				OperateLogEntity::getErrorMsg, OperateLogEntity::getRequestMethod,
				OperateLogEntity::getRequestUri, OperateLogEntity::getIp, OperateLogEntity::getClassName,
				OperateLogEntity::getMethodName, OperateLogEntity::getDurationMs,
				OperateLogEntity::getUsername, OperateLogEntity::getTraceId, OperateLogEntity::getCreateTime);
		if (restrictedUserId != null) {
			qw.eq(OperateLogEntity::getUserId, restrictedUserId);
		}
		if (StringUtils.hasText(form.getKeyword())) {
			String keyword = form.getKeyword().trim();
			qw.and(condition -> condition
					.like(OperateLogEntity::getRequestUri, keyword)
					.or()
					.like(OperateLogEntity::getMethodName, keyword)
					.or()
					.like(OperateLogEntity::getBizName, keyword)
					.or()
					.like(OperateLogEntity::getUsername, keyword));
		}
		if (form.getSuccess() != null) {
			qw.eq(OperateLogEntity::getSuccess, form.getSuccess());
		}
		if (StringUtils.hasText(form.getTraceId())) {
			qw.eq(OperateLogEntity::getTraceId, form.getTraceId().trim());
		}
		LocalDateTime effectiveBeginTime = LogQueryValidator.resolveRestrictedBeginTime(
				form.getBeginTime(), restrictedBeginTime);
		if (effectiveBeginTime != null) {
			qw.ge(OperateLogEntity::getCreateTime, effectiveBeginTime);
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

	public OperateLogDetailVO detail(Long id) {
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
