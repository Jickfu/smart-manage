package sm.domain.sys.base.sysparam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.sysparam.model.entity.SysParamEntity;
import sm.domain.sys.base.sysparam.model.form.SysParamSaveForm;
import sm.domain.sys.base.sysparam.mapper.SysParamMapper;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/**
 * 系统参数事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class SysParamTxService {
    private final SysParamMapper mapper;
    private final FeatureMapper featureMapper;

    /** 新增/编辑，清除缓存 */
    public Long save(SysParamSaveForm form) {
        SysParamEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "系统参数不存在");
            }
            if (form.getVersion() == null) {
                throw new BizException(ResultEnum.PARAM_ERROR, "修改系统参数时乐观锁版本号不能为空");
            }
            if (!java.util.Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "系统参数已被其他用户修改，请刷新后重试");
            }
            // 系统内置参数只允许修改 value
            if (Boolean.TRUE.equals(entity.getIsSystem())) {
                entity.setValue(form.getValue());
                if (mapper.updateById(entity) == 0) {
                    throw new BizException(ResultEnum.DATA_CONFLICT, "系统参数已被其他用户修改，请刷新后重试");
                }
                return entity.getId();
            }
        } else {
            entity = new SysParamEntity();
        }
        Long duplicateCount = mapper.selectCount(new LambdaQueryWrapper<SysParamEntity>()
                .eq(SysParamEntity::getNumber, form.getNumber())
                .ne(form.getId() != null, SysParamEntity::getId, form.getId()));
        if (duplicateCount > 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "系统参数编码已存在");
        }
        if (form.getFeatureId() != null && featureMapper.selectById(form.getFeatureId()) == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "所属功能不存在");
        }
        entity.setNumber(form.getNumber());
        entity.setName(form.getName());
        entity.setValue(form.getValue());
        entity.setDescription(form.getDescription());
        entity.setFeatureId(form.getFeatureId());
        if (form.getId() == null) {
            entity.setIsSystem(false);
        }
        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            if (mapper.updateById(entity) == 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "系统参数已被其他用户修改，请刷新后重试");
            }
        }
        return entity.getId();
    }

    /** 删除，清除缓存 */
    public void deleteById(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "系统参数ID不能为空");
        }
        SysParamEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "系统参数不存在");
        }
        if (Boolean.TRUE.equals(entity.getIsSystem())) {
            throw new BizException(ResultEnum.BILL_STATUS_ERROR, "系统内置参数不可删除");
        }
        if (mapper.deleteById(id) == 0) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "系统参数已被其他用户删除");
        }
    }
}
