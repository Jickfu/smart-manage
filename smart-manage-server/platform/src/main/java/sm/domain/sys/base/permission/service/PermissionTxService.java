package sm.domain.sys.base.permission.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.domain.sys.base.permission.model.form.PermissionSaveForm;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.feature.service.FeatureReferenceService;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/**
 * 权限事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class PermissionTxService {
    private final PermissionMapper mapper;
    private final FeatureReferenceService featureReferenceService;

    public Long save(PermissionSaveForm form) {
        PermissionEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "权限不存在");
            }
            if (form.getVersion() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "修改权限时乐观锁版本号不能为空");
            }
            if (!java.util.Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "权限已被其他用户修改，请刷新后重试");
            }
        } else {
            entity = new PermissionEntity();
        }
        entity.setName(form.getName());
        entity.setNumber(form.getNumber());
        featureReferenceService.require(form.getFeatureId());
        entity.setFeatureId(form.getFeatureId());
        entity.setAppId(null);
        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "权限已被其他用户修改，请刷新后重试");
            }
        }
        return entity.getId();
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "权限ID不能为空");
        }
        PermissionEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "权限不存在");
        }
        if (mapper.deleteById(id) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "权限已被其他用户删除");
        }
    }
}
