package sm.domain.sys.monitor.operatelog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sm.domain.sys.monitor.operatelog.mapper.OperateLogMapper;
import sm.domain.sys.monitor.operatelog.model.entity.OperateLogEntity;

/** 操作日志模块拥有的内部写入边界。 */
@Service
@RequiredArgsConstructor
public class OperateLogPersistenceService {
    private final OperateLogMapper mapper;

    public void write(OperateLogEntity entity) {
        mapper.insert(entity);
    }
}
