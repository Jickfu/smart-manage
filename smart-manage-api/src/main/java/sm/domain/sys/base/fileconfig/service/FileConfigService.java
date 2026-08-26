package sm.domain.sys.base.fileconfig.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
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
import sm.system.security.authorization.AdministratorOnly;
import sm.system.security.authorization.AdministratorOnly;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean defaultStorageWarningLogged = new AtomicBoolean();
    @Value("${smart-manage.system.upload.dir:./upload/}")
    private String defaultLocalDir = "./upload/";

    /** 单例管理页读取；尚未配置时返回本地存储默认值。 */
    public FileConfigDetailVO singleton() {
        List<FileConfigEntity> entityList = mapper.selectList(null);
        if (entityList.isEmpty()) {
            FileConfigDetailVO detail = new FileConfigDetailVO();
            detail.setStorageType("LOCAL");
            detail.setLocalDir(defaultLocalDir);
            detail.setFtpPort(21);
            detail.setFtpPassiveMode(true);
            detail.setFtpPasswordConfigured(false);
            return detail;
        }
        return converter.toDetailVO(entityList.get(0));
    }

    /** 获取服务端内部使用的活跃配置，敏感字段不得通过 Controller 暴露。 */
    @Override
    public FileStorageConfig getFileStorageConfig() {
        List<FileConfigEntity> entityList = mapper.selectList(null);
        if (entityList.isEmpty()) {
            if (defaultStorageWarningLogged.compareAndSet(false, true)) {
                log.warn("文件存储尚未持久化配置，当前使用显式部署目录的 Local 默认配置；多实例部署前必须改为 S3 或 FTP");
            }
            return new FileStorageConfig("LOCAL", defaultLocalDir, null, null, null, null, null, null,
                    null, null, null, null, null, null);
        }
        FileConfigEntity entity = entityList.get(0);
        String ftpPassword = entity.getFtpPasswordCipher() == null
                ? null : sm4Helper.decrypt(entity.getFtpPasswordCipher());
        String s3SecretKey = entity.getS3SecretKeyCipher() == null
                ? null : sm4Helper.decrypt(entity.getS3SecretKeyCipher());
        return new FileStorageConfig(
                entity.getStorageType(), entity.getLocalDir(), entity.getFtpHost(), entity.getFtpPort(),
                entity.getFtpUsername(), ftpPassword, entity.getFtpDir(), entity.getFtpPassiveMode(),
                entity.getS3Endpoint(), entity.getS3Region(), entity.getS3Bucket(), entity.getS3AccessKey(),
                s3SecretKey, entity.getS3PathStyle());
    }

    @BizLog("保存文件存储配置")
    @AdministratorOnly
    public Long save(FileConfigSaveForm form) {
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
        if ("S3".equalsIgnoreCase(form.getStorageType())) {
            if (form.getS3Endpoint() == null || form.getS3Endpoint().isBlank()
                    || form.getS3Region() == null || form.getS3Region().isBlank()
                    || form.getS3Bucket() == null || form.getS3Bucket().isBlank()
                    || form.getS3AccessKey() == null || form.getS3AccessKey().isBlank()) {
                throw new BizException(ResultEnum.PARAM_ERROR, "S3 Endpoint、Region、Bucket 和 Access Key 不能为空");
            }
            boolean secretConfigured = false;
            if (form.getId() != null) {
                FileConfigEntity existing = mapper.selectById(form.getId());
                secretConfigured = existing != null && existing.getS3SecretKeyCipher() != null;
            }
            if (!secretConfigured && (form.getS3SecretKey() == null || form.getS3SecretKey().isBlank())) {
                throw new BizException(ResultEnum.PARAM_ERROR, "S3 Secret Key 不能为空");
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
                || !Objects.equals(existing.getFtpDir(), form.getFtpDir())
                || !Objects.equals(existing.getS3Endpoint(), form.getS3Endpoint())
                || !Objects.equals(existing.getS3Bucket(), form.getS3Bucket());
        if (topologyChanged) {
            throw new BizException(ResultEnum.CONFIG_ERROR,
                    "系统已有附件，不能直接切换存储类型、根目录或FTP主机；请先完成文件迁移");
        }
    }

    /**
     * 使用前端当前填写的参数测试 FTP 连通性，不读取也不保存文件配置。
     */
    @BizLog(value = "测试FTP连接", recordRequest = false)
    @AdministratorOnly
    public String testFtp(FtpTestForm form) {
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
