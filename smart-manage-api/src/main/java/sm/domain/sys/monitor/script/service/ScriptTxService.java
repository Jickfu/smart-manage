package sm.domain.sys.monitor.script.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.monitor.script.mapper.ScriptMapper;
import sm.domain.sys.monitor.script.model.entity.ScriptEntity;
import sm.domain.sys.monitor.script.model.form.ScriptDeleteForm;
import sm.domain.sys.monitor.script.model.form.ScriptSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class ScriptTxService {
    private final ScriptMapper mapper;

    Long save(ScriptSaveForm form) {
        ScriptEntity duplicate = mapper.selectOne(new LambdaQueryWrapper<ScriptEntity>()
                .eq(ScriptEntity::getNumber, form.getNumber().trim())
                .ne(form.getId() != null, ScriptEntity::getId, form.getId()));
        if (duplicate != null) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "脚本编码已存在");
        }
        ScriptEntity entity = form.getId() == null ? new ScriptEntity() : require(form.getId());
        if (form.getId() != null && !Objects.equals(entity.getVersion(), form.getVersion())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "脚本已被修改，请刷新后重试");
        }
        entity.setNumber(form.getNumber().trim());
        entity.setName(form.getName().trim());
        entity.setContent(form.getContent());
        entity.setRemark(form.getRemark());
        try {
            int affected = form.getId() == null ? mapper.insert(entity) : mapper.updateById(entity);
            if (affected != 1) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "脚本保存失败，请刷新后重试");
            }
        } catch (DuplicateKeyException exception) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "脚本编码已存在");
        }
        return entity.getId();
    }

    void delete(ScriptDeleteForm form) {
        ScriptEntity entity = require(form.getId());
        if (!Objects.equals(entity.getVersion(), form.getVersion())) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "脚本已被修改，请刷新后重试");
        }
        if (mapper.deleteById(entity) != 1) {
            throw new BizException(ResultEnum.DATA_CONFLICT, "脚本已被修改或删除");
        }
    }

    private ScriptEntity require(Long id) {
        ScriptEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "脚本不存在");
        }
        return entity;
    }
}
