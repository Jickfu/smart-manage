package sm.domain.sys.monitor.loginlog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.loginlog.mapper.LoginLogMapper;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;

/** 登录日志模块拥有的内部写入边界。 */
@Service
@RequiredArgsConstructor
public class LoginLogPersistenceService {
    private final LoginLogMapper mapper;

    public void write(LoginLogEntity entity) {
        mapper.insert(entity);
    }
}
