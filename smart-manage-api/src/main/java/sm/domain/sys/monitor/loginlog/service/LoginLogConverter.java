package sm.domain.sys.monitor.loginlog.service;

import org.mapstruct.Mapper;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogDetailVO;
import sm.domain.sys.monitor.loginlog.model.vo.LoginLogListVO;
import sm.framework.mapping.SmMapperConfig;

/** 登录日志纯字段转换器。 */
@Mapper(config = SmMapperConfig.class)
interface LoginLogConverter {
    LoginLogListVO toListVO(LoginLogEntity entity);
    LoginLogDetailVO toDetailVO(LoginLogEntity entity);
}
