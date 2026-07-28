package sm.domain.sys.base.fileconfig.service;

import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import sm.domain.sys.base.common.constant.CacheConstant;
import sm.domain.sys.base.fileconfig.model.entity.FileConfigEntity;
import sm.domain.sys.base.fileconfig.model.form.FileConfigListForm;
import sm.domain.sys.base.fileconfig.model.form.FileConfigSaveForm;
import sm.domain.sys.base.fileconfig.model.form.FtpTestForm;
import sm.domain.sys.base.fileconfig.model.vo.FileConfigDetailVO;
import sm.domain.sys.base.fileconfig.mapper.FileConfigMapper;
import sm.system.exception.BizException;
import sm.system.aop.log.BizLog;
import sm.system.response.PageData;
import sm.system.response.ResultEnum;
import sm.system.helper.SM4Helper;
import sm.system.storage.FileStorageConfig;
import sm.system.storage.FileStorageConfigProvider;
import sm.domain.sys.base.common.helper.UserHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public PageData<FileConfigDetailVO> listPage(FileConfigListForm form) {
        LambdaQueryWrapper<FileConfigEntity> qw = new LambdaQueryWrapper<FileConfigEntity>();
        if (form.getKeyword() != null && !form.getKeyword().isBlank()) {
            String kw = form.getKeyword().trim();
            qw.like(FileConfigEntity::getStorageType, kw);
        }
        qw.orderByAsc(FileConfigEntity::getId);
        Page<FileConfigEntity> page = new Page<>(form.getPageNum(), form.getPageSize());
        Page<FileConfigEntity> result = mapper.selectPage(page, qw);
        List<FileConfigDetailVO> vos = result.getRecords().stream().map(converter::toDetailVO).toList();
        return PageData.of(result.getTotal(), form.getPageNum(), form.getPageSize(), vos);
    }

    public FileConfigDetailVO detail(Long id) {
        if (id == null) {
            throw new BizException(ResultEnum.PARAM_ERROR, "文件配置ID不能为空");
        }
        FileConfigEntity entity = mapper.selectById(id);
        if (entity == null) {
            throw new BizException(ResultEnum.NOT_FOUND, "文件配置不存在");
        }
        return converter.toDetailVO(entity);
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
        return txService.save(form);
    }

    @BizLog("删除文件存储配置")
    @CacheInvalidate(name = CacheConstant.FILE_CONFIG,
            key = "T(sm.domain.sys.base.common.constant.CacheConstant).SINGLETON_KEY")
    public void deleteById(Long id) {
        txService.deleteById(id);
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
