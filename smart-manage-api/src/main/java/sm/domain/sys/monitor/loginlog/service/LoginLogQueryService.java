package sm.domain.sys.monitor.loginlog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.domain.sys.monitor.loginlog.model.form.LoginLogListForm;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogListVO;
import sm.domain.sys.monitor.loginlog.mapper.LoginLogMapper;
import sm.system.exception.BizException;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;


@Service
@RequiredArgsConstructor
public class LoginLogQueryService {
	private final LoginLogMapper loginLogMapper;
	private final LoginLogConverter converter;

	public PageData<LoginLogListVO> listPage(LoginLogListForm form) {
		LambdaQueryWrapper<LoginLogEntity> qw = new LambdaQueryWrapper<LoginLogEntity>();
		if (StringUtils.hasText(form.getKeyword())) {
			String kw = "%" + form.getKeyword().trim() + "%";
			qw.and(condition -> condition.like(LoginLogEntity::getUsername, kw).or().like(LoginLogEntity::getNickname, kw));
		}
		if (form.getSuccess() != null) {
			qw.eq(LoginLogEntity::getSuccess, form.getSuccess());
		}
		if (form.getBeginTime() != null) {
			qw.ge(LoginLogEntity::getCreateTime, form.getBeginTime());
		}
		if (form.getEndTime() != null) {
			qw.le(LoginLogEntity::getCreateTime, form.getEndTime());
		}
		qw.orderByDesc(LoginLogEntity::getCreateTime);
		Page<LoginLogEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
		Page<LoginLogEntity> result = loginLogMapper.selectPage(page, qw);
		var records = result.getRecords().stream().map(converter::toListVO).toList();
		return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), records);
	}

	public LoginLogListVO detail(Long id) {
		if (id == null) {
			throw new BizException(ResultEnum.PARAM_ERROR, "登录日志ID不能为空");
		}
		LoginLogEntity entity = loginLogMapper.selectById(id);
		if (entity == null) {
			throw new BizException(ResultEnum.NOT_FOUND, "登录日志不存在");
		}
		return converter.toListVO(entity);
	}
}
