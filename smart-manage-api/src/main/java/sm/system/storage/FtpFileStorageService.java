package sm.system.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * FTP 文件存储实现
 *
 * @author Chekfu
 */
@Component
@Slf4j
public class FtpFileStorageService implements FileStorageService {

    private final FileStorageConfigProvider configProvider;

    public FtpFileStorageService(FileStorageConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    private static final String TEMP_DIR = "temp";

    private FileStorageConfig config() {
        return configProvider.getFileStorageConfig();
    }

    private FTPClient connect() throws IOException {
        FileStorageConfig cfg = config();
        FTPClient ftp = createClient();
        ftp.connect(cfg.ftpHost(), cfg.ftpPort() != null ? cfg.ftpPort() : 21);
        if (!ftp.login(cfg.ftpUsername(), cfg.ftpPassword())) {
            disconnect(ftp);
            throw new IOException("FTP 登录失败: " + ftp.getReplyString());
        }
        if (Boolean.TRUE.equals(cfg.ftpPassiveMode())) {
            ftp.enterLocalPassiveMode();
        }
        if (!ftp.setFileType(FTP.BINARY_FILE_TYPE)) {
            disconnect(ftp);
            throw new IOException("FTP 二进制模式设置失败: " + ftp.getReplyString());
        }
        ftp.setBufferSize(1024 * 1024);
        String remoteDir = cfg.ftpDir();
        if (remoteDir != null && !remoteDir.isBlank()) {
            ensureDirectories(ftp, remoteDir);
            if (!ftp.changeWorkingDirectory(remoteDir)) {
                disconnect(ftp);
                throw new IOException("FTP 根目录切换失败: " + remoteDir + ", " + ftp.getReplyString());
            }
        }
        return ftp;
    }

    /**
     * 创建相对当前目录的层级目录，结束后恢复调用前工作目录。
     */
    private void ensureDirectories(FTPClient ftp, String path) throws IOException {
        String originalDirectory = ftp.printWorkingDirectory();
        if (originalDirectory == null) {
            throw new IOException("FTP 无法读取当前工作目录: " + ftp.getReplyString());
        }
        try {
            for (String part : path.replace('\\', '/').split("/")) {
                if (part.isEmpty()) {
                    continue;
                }
                if (".".equals(part) || "..".equals(part)) {
                    throw new IOException("FTP 目录不允许包含相对路径片段: " + path);
                }
                if (!ftp.changeWorkingDirectory(part)) {
                    if (!ftp.makeDirectory(part) || !ftp.changeWorkingDirectory(part)) {
                        throw new IOException("FTP 目录创建失败: " + path + ", " + ftp.getReplyString());
                    }
                }
            }
        } finally {
            if (!ftp.changeWorkingDirectory(originalDirectory)) {
                throw new IOException("FTP 工作目录恢复失败: " + originalDirectory + ", " + ftp.getReplyString());
            }
        }
    }

    /**
     * 允许测试替换 FTP 客户端，不暴露到业务调用层。
     */
    protected FTPClient createClient() {
        return new FTPClient();
    }

    private void disconnect(FTPClient ftp) {
        if (ftp != null && ftp.isConnected()) {
            try {
                ftp.logout();
                ftp.disconnect();
            } catch (IOException ignored) { /* ignore */ }
        }
    }

    @Override
    public FileStoreResult store(String subDir, MultipartFile file) throws IOException {
        return doStore(file, subDir);
    }

    @Override
    public FileStoreResult storeTemp(MultipartFile file) throws IOException {
        return doStore(file, TEMP_DIR);
    }

    private FileStoreResult doStore(MultipartFile file, String subDir) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + ext;
        FTPClient ftp = connect();
        try {
            if (subDir != null && !subDir.isEmpty()) {
                ensureDirectories(ftp, subDir);
                if (!ftp.changeWorkingDirectory(subDir)) {
                    throw new IOException("FTP 上传目录切换失败: " + subDir + ", " + ftp.getReplyString());
                }
            }
            String remotePath = subDir != null && !subDir.isEmpty()
                    ? subDir + "/" + storedName : storedName;
            try (InputStream is = file.getInputStream()) {
                if (!ftp.storeFile(storedName, is)) {
                    throw new IOException("FTP 上传失败: " + ftp.getReplyString());
                }
            }
            log.info("FTP 文件存储: {}", remotePath);
            return FileStoreResult.of(storedName, remotePath, file.getSize());
        } finally {
            disconnect(ftp);
        }
    }

    @Override
    public String move(String storedPath, String targetSubDir) throws IOException {
        String filename = storedPath.contains("/") ? storedPath.substring(storedPath.lastIndexOf("/") + 1) : storedPath;
        FTPClient ftp = connect();
        try {
            ensureDirectories(ftp, targetSubDir);
            String targetFull = targetSubDir + "/" + filename;
            if (!ftp.rename(storedPath, targetFull)) {
                throw new IOException("FTP 文件移动失败: " + ftp.getReplyString());
            }
            log.info("FTP 文件移动: {} -> {}", storedPath, targetFull);
            return targetFull;
        } finally {
            disconnect(ftp);
        }
    }

    @Override
    public void delete(String storedPath) throws IOException {
        if (storedPath == null) return;
        FTPClient ftp = connect();
        try {
            if (!ftp.deleteFile(storedPath)) {
                throw new IOException("FTP 文件删除失败: " + storedPath + ", " + ftp.getReplyString());
            }
            log.info("FTP 文件删除: {}", storedPath);
        } finally {
            disconnect(ftp);
        }
    }

    @Override
    public byte[] getBytes(String storedPath) throws IOException {
        FTPClient ftp = connect();
        try {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                if (!ftp.retrieveFile(storedPath, bos)) {
                    throw new IOException("FTP 下载失败: " + ftp.getReplyString());
                }
                return bos.toByteArray();
            }
        } finally {
            disconnect(ftp);
        }
    }

    @Override
    public String getAccessUrl(String storedPath) {
        return null;
    }

    @Override
    public String getType() {
        return "FTP";
    }
}
