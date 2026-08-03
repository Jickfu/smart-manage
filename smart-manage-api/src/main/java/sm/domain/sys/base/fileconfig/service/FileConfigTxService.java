package sm.domain.sys.base.fileconfig.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.domain.sys.base.fileconfig.model.form.FileConfigSaveForm;
import sm.domain.sys.base.fileconfig.mapper.FileConfigMapper;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;
import sm.system.helper.SM4Helper;

import java.util.Objects;

/**
 * 文件配置事务服务 —— 所有写操作在类级别事务中执行
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class FileConfigTxService {
    private final FileConfigMapper mapper;
    private final SM4Helper sm4Helper;

    public Long save(FileConfigSaveForm form) {
        FileConfigEntity entity;
        if (form.getId() != null) {
            entity = mapper.selectById(form.getId());
            if (entity == null) {
                throw new BizException(ResultEnum.NOT_FOUND, "文件配置不存在");
            }
            if (form.getVersion() == null || !Objects.equals(entity.getVersion(), form.getVersion())) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "文件配置已被其他用户修改，请刷新后重试");
            }
        } else {
            if (mapper.selectCount(null) > 0) {
                throw new BizException(ResultEnum.DATA_CONFLICT, "文件配置为单例，不能重复新增");
            }
            entity = new FileConfigEntity();
        }
        entity.setStorageType(form.getStorageType());
        entity.setLocalDir(form.getLocalDir());
        entity.setFtpHost(form.getFtpHost());
        entity.setFtpPort(form.getFtpPort());
        entity.setFtpUsername(form.getFtpUsername());
        if (form.getFtpPassword() != null && !form.getFtpPassword().isBlank()) {
            entity.setFtpPasswordCipher(sm4Helper.encrypt(form.getFtpPassword()));
        }
        entity.setFtpDir(form.getFtpDir());
        entity.setFtpPassiveMode(form.getFtpPassiveMode());
        if (form.getId() == null) {
            if (mapper.insert(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.PERSISTENCE_ERROR, "新增数据失败");
            }
        } else {
            if (mapper.updateById(entity) != 1) {
                throw new BizException(sm.system.response.ResultEnum.DATA_CONFLICT, "数据已被其他用户修改");
            }
        }
        return entity.getId();
    }

}
