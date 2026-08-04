package sm.domain.sys.monitor.script.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.monitor.script.mapper.ScriptLogMapper;
import sm.domain.sys.monitor.script.model.entity.ScriptLogEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class ScriptExecutionLogTxService {
    private final ScriptLogMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    void save(ScriptLogEntity entity) {
        if (mapper.insert(entity) != 1) {
            throw new BizException(ResultEnum.PERSISTENCE_ERROR, "脚本执行审计写入失败");
        }
    }
}
