package sm.domain.sys.base.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.domain.model.entity.DomainEntity;
import sm.domain.sys.base.domain.model.form.DomainSaveForm;
import sm.domain.sys.base.domain.mapper.DomainMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.util.EnabledCommandUtil;

import java.util.List;

/**
 * 领域事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class DomainTxService {
    private final DomainMapper mapper;

    public Long save(DomainSaveForm form) {
        DomainEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "领域不存在");
            }
            if (form.getVersion() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "修改领域时乐观锁版本号不能为空");
            }
            if (!java.util.Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "领域已被其他用户修改，请刷新后重试");
            }
        } else {
            entity = new DomainEntity();
        }
        entity.setName(form.getName());
        entity.setNumber(form.getNumber());
        entity.setSeq(form.getSeq() != null ? form.getSeq() : 99);
        if (form.getId() == null) {
            entity.setEnabled(true);
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "领域已被其他用户修改，请刷新后重试");
            }
        }
        return entity.getId();
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "领域ID不能为空");
        }
        DomainEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "领域不存在");
        }
        if (mapper.deleteById(id) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "领域已被其他用户删除");
        }
    }

    public void updateEnabled(List<Long> ids, boolean enabled) {
        EnabledCommandUtil.update(mapper, DomainEntity::getId, DomainEntity::getEnabled, ids, enabled, "领域");
    }
}
