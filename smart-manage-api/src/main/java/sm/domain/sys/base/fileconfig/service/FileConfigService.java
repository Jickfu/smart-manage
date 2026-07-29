package sm.domain.sys.base.fileconfig.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheInvalidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.domain.sys.base.fileconfig.model.form.FileConfigSaveForm;
import sm.domain.sys.base.fileconfig.model.form.FtpTestForm;
import sm.domain.sys.base.fileconfig.model.vo.FileConfigDetailVO;
import sm.domain.sys.base.fileconfig.mapper.FileConfigMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.ResultEnum;
import sm.system.helper.SM4Helper;
import sm.system.storage.FileStorageConfig;
import sm.system.storage.FileStorageConfigProvider;
import sm.domain.sys.base.common.helper.UserHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Objects;

/**
 * 文件配置服务
 *
 * @author Chekfu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileConfigService implements FileStorageConfigProvider {
    private final FileConfigMapper mapper;
    private final FileConfigTxService txService;
    private final SM4Helper sm4Helper;
    private final FileConfigConverter converter;

    /** 单例管理页读取；尚未配置时返回本地存储默认值。 */
    public FileConfigDetailVO singleton() {
        List<FileConfigEntity> entityList = mapper.selectList(null);
        if (entityList.isEmpty()) {
            FileConfigDetailVO detail = new FileConfigDetailVO();
            detail.setStorageType("LOCAL");
            detail.setLocalDir("E:/upload/");
            detail.setFtpPort(21);
            detail.setFtpPassiveMode(true);
            detail.setFtpPasswordConfigured(false);
            return detail;
        }
        return converter.toDetailVO(entityList.get(0));
    }

    /** 获取服务端内部使用的活跃配置，敏感字段不得通过 Controller 暴露。 */
    @Override
    @Cached(cacheType = CacheType.LOCAL, name = CacheConstant.FILE_CONFIG,
            key = "T(sm.domain.sys.base.common.constant.CacheConstant).SINGLETON_KEY",
            expire = 30, timeUnit = TimeUnit.MINUTES)
    public FileStorageConfig getFileStorageConfig() {
        List<FileConfigEntity> entityList = mapper.selectList(null);
        if (entityList.isEmpty()) {
            return new FileStorageConfig("LOCAL", "E:/upload/", null, null, null, null, null, null);
        }
        FileConfigEntity entity = entityList.get(0);
        String ftpPassword = entity.getFtpPasswordCipher() == null
                ? null : sm4Helper.decrypt(entity.getFtpPasswordCipher());
        return new FileStorageConfig(
                entity.getStorageType(), entity.getLocalDir(), entity.getFtpHost(), entity.getFtpPort(),
                entity.getFtpUsername(), ftpPassword, entity.getFtpDir(), entity.getFtpPassiveMode());
    }

    @BizLog("保存文件存储配置")
    @CacheInvalidate(name = CacheConstant.FILE_CONFIG,
            key = "T(sm.domain.sys.base.common.constant.CacheConstant).SINGLETON_KEY")
    public Long save(FileConfigSaveForm form) {
        // 存储目录和凭据影响全系统文件读写，除权限码外必须校验管理员身份。
        UserHelper.checkAdmin();
        validateStorageConfig(form);
        validateStorageTopologyChange(form);
        return txService.save(form);
    }

    private void validateStorageConfig(FileConfigSaveForm form) {
        if ("LOCAL".equalsIgnoreCase(form.getStorageType())) {
            if (form.getLocalDir() == null || form.getLocalDir().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "本地存储目录不能为空");
            }
            return;
        }
        if (!"FTP".equalsIgnoreCase(form.getStorageType())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "不支持的文件存储类型: " + form.getStorageType());
        }
        if (form.getFtpHost() == null || form.getFtpHost().isBlank()
                || form.getFtpPort() == null
                || form.getFtpUsername() == null || form.getFtpUsername().isBlank()) {
            throw new BizException(ResultEnum.PARAM_ERROR, "FTP 主机、端口和用户名不能为空");
        }
        boolean passwordConfigured = false;
        if (form.getId() != null) {
            FileConfigEntity existing = mapper.selectById(form.getId());
            passwordConfigured = existing != null && existing.getFtpPasswordCipher() != null;
        }
        if (!passwordConfigured && (form.getFtpPassword() == null || form.getFtpPassword().isBlank())) {
            throw new BizException(ResultEnum.PARAM_ERROR, "FTP 密码不能为空");
        }
    }

    /**
     * 当前版本不提供跨存储迁移；已有附件时禁止改变会导致历史路径失效的存储拓扑。
     * 凭据、端口和被动模式仍可按运维需要调整。
     */
    private void validateStorageTopologyChange(FileConfigSaveForm form) {
        if (form.getId() == null || !mapper.existsStoredAttachment()) {
            return;
        }
        FileConfigEntity existing = mapper.selectById(form.getId());
        if (existing == null) {
            return;
        }
        boolean topologyChanged = !Objects.equals(existing.getStorageType(), form.getStorageType())
                || !Objects.equals(existing.getLocalDir(), form.getLocalDir())
                || !Objects.equals(existing.getFtpHost(), form.getFtpHost())
                || !Objects.equals(existing.getFtpDir(), form.getFtpDir());
        if (topologyChanged) {
            throw new BizException(ResultEnum.CONFIG_ERROR,
                    "系统已有附件，不能直接切换存储类型、根目录或FTP主机；请先完成文件迁移");
        }
    }

    /**
     * 使用前端当前填写的参数测试 FTP 连通性，不读取也不保存文件配置。
     */
    @BizLog(value = "测试FTP连接", recordRequest = false)
    public String testFtp(FtpTestForm form) {
        // FTP 连接可访问任意网络地址，除业务权限外还必须校验超级管理员账号身份。
        UserHelper.checkAdmin();
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(form.getFtpHost(), form.getFtpPort());
            if (!ftpClient.login(form.getFtpUsername(), form.getFtpPassword())) {
                throw new BizException(ResultEnum.CONFIG_ERROR, "FTP 登录失败: " + ftpClient.getReplyString());
            }
            if (Boolean.TRUE.equals(form.getFtpPassiveMode())) {
                ftpClient.enterLocalPassiveMode();
            }
            if (form.getFtpDir() != null && !form.getFtpDir().isBlank()
                    && !ftpClient.changeWorkingDirectory(form.getFtpDir())) {
                throw new BizException(ResultEnum.CONFIG_ERROR, "FTP 目录切换失败: " + ftpClient.getReplyString());
            }
            ftpClient.logout();
            return "FTP 连接成功";
        } catch (BizException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BizException(ResultEnum.CONFIG_ERROR, "FTP 连接失败: " + exception.getMessage());
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (Exception exception) {
                    log.warn("关闭 FTP 测试连接失败", exception);
                }
            }
        }
    }
}
