package sm.domain.sys.monitor.loginlog.service;

import sm.domain.sys.monitor.loginlog.converter.LoginLogConverter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.Map;
import sm.domain.sys.monitor.common.util.LogQueryValidator;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.domain.sys.monitor.loginlog.model.form.LoginLogListForm;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogDetailVO;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogListVO;
import sm.domain.sys.monitor.loginlog.mapper.LoginLogMapper;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.query.ListQueryUtil;

import static sm.system.query.ListQueryUtil.bool;
import static sm.system.query.ListQueryUtil.dateTime;
import static sm.system.query.ListQueryUtil.enumeration;
import static sm.system.query.ListQueryUtil.number;
import static sm.system.query.ListQueryUtil.string;

@Service
@RequiredArgsConstructor
public class LoginLogService {
	private static final Map<String, ListQueryUtil.Field<LoginLogEntity>> LIST_FIELDS = Map.of(
			"id", number(LoginLogEntity::getId, true),
			"username", string(LoginLogEntity::getUsername, false),
			"nickname", string(LoginLogEntity::getNickname, false),
			"eventType", enumeration(LoginLogEntity::getEventType, false),
			"success", bool(LoginLogEntity::getSuccess, false),
			"ip", string(LoginLogEntity::getIp, false),
			"createTime", dateTime(LoginLogEntity::getCreateTime, true),
			"traceId", string(LoginLogEntity::getTraceId, false));
	private final LoginLogMapper loginLogMapper;
	private final LoginLogConverter converter;

	public PageData<LoginLogListVO> listPage(LoginLogListForm form) {
		return listPage(form, null, null);
	}

	/** 当前账号入口强制追加用户条件，不能依赖前端传入用户名进行数据隔离。 */
	public PageData<LoginLogListVO> listCurrentPage(LoginLogListForm form, Long currentUserId) {
		if (currentUserId == null) {
			throw new BizException(ResultEnum.UNAUTHORIZED, "当前用户未登录");
		}
		return listPage(form, currentUserId, LocalDateTime.now().minusDays(7));
	}

	private PageData<LoginLogListVO> listPage(
			LoginLogListForm form, Long restrictedUserId, LocalDateTime restrictedBeginTime) {
		LogQueryValidator.validateTimeRange(form.getBeginTime(), form.getEndTime());
		LambdaQueryWrapper<LoginLogEntity> qw = new LambdaQueryWrapper<LoginLogEntity>();
		// 列表不读取 User-Agent 等详情字段，避免大字段放大分页 IO。
		qw.select(LoginLogEntity::getId, LoginLogEntity::getUserId, LoginLogEntity::getUsername,
				LoginLogEntity::getNickname, LoginLogEntity::getEventType, LoginLogEntity::getSuccess,
				LoginLogEntity::getFailReason, LoginLogEntity::getIp, LoginLogEntity::getTraceId,
				LoginLogEntity::getIssuerUserId, LoginLogEntity::getGrantId,
				LoginLogEntity::getCreateTime);
		if (restrictedUserId != null) {
			qw.eq(LoginLogEntity::getUserId, restrictedUserId);
		}
		if (StringUtils.hasText(form.getKeyword())) {
			String keyword = form.getKeyword().trim();
			qw.and(condition -> condition
					.like(LoginLogEntity::getUsername, keyword)
					.or()
					.like(LoginLogEntity::getNickname, keyword)
					.or()
					.like(LoginLogEntity::getIp, keyword));
		}
		if (form.getSuccess() != null) {
			qw.eq(LoginLogEntity::getSuccess, form.getSuccess());
		}
		if (form.getEventType() != null) {
			qw.eq(LoginLogEntity::getEventType, form.getEventType().name());
		}
		if (StringUtils.hasText(form.getTraceId())) {
			qw.eq(LoginLogEntity::getTraceId, form.getTraceId().trim());
		}
		LocalDateTime effectiveBeginTime = LogQueryValidator.resolveRestrictedBeginTime(
				form.getBeginTime(), restrictedBeginTime);
		if (effectiveBeginTime != null) {
			qw.ge(LoginLogEntity::getCreateTime, effectiveBeginTime);
		}
		if (form.getEndTime() != null) {
			qw.le(LoginLogEntity::getCreateTime, form.getEndTime());
		}
		ListQueryUtil.apply(qw, form, LIST_FIELDS);
		if (!ListQueryUtil.hasSort(form)) {
			qw.orderByDesc(LoginLogEntity::getCreateTime);
		}
		if (!ListQueryUtil.isSortedBy(form, "id")) {
			qw.orderByDesc(LoginLogEntity::getId);
		}
		Page<LoginLogEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<LoginLogEntity> result = loginLogMapper.selectPage(page, qw);
		var records = result.getRecords().stream().map(converter::toListVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
	}

	public LoginLogDetailVO detail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "登录日志ID不能为空");
		}
		LoginLogEntity entity = loginLogMapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "登录日志不存在");
		}
		return converter.toDetailVO(entity);
	}
}
