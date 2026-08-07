package sm.system.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件存储实现
 * <p>
 * 目录结构：
 * <pre>
 * {baseDir}/
 * ├── temp/          # 临时上传
 * ├── sys/           # 系统配置（界面配置图片等）
 * ├── biz/           # 业务单据
 * │   └── {bizType}/ # 不同业务类型
 * └── other/         # 其他
 * </pre>
 *
 * @author Chekfu
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final FileStorageConfigProvider configProvider;

    private static final String TEMP_DIR = "temp";

    private String getBaseDir() {
        String dir = configProvider.getFileStorageConfig().localDir();
        if (dir == null || dir.isBlank()) {
            dir = "E:/upload/";
        }
        if (!dir.endsWith("/") && !dir.endsWith("\\")) {
            dir = dir + "/";
        }
        return dir;
    }

    @Override
    public FileStoreResult store(String subDir, MultipartFile file) throws IOException {
        return doStore(file, getBaseDir() + subDir + "/");
    }

    @Override
    public FileStoreResult storeTemp(MultipartFile file) throws IOException {
        return doStore(file, getBaseDir() + TEMP_DIR + "/");
    }

    private FileStoreResult doStore(MultipartFile file, String dir) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + ext;
        Path dirPath = Paths.get(dir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        Path filePath = dirPath.resolve(storedName);
        file.transferTo(filePath.toFile());
        log.info("本地文件存储: {}", filePath);
        return FileStoreResult.of(storedName, filePath.toString(), file.getSize());
    }

    @Override
    public String move(String storedPath, String targetSubDir) throws IOException {
        Path source = Paths.get(storedPath);
        if (!Files.exists(source)) {
            throw new IOException("待移动文件不存在: " + storedPath);
        }
        Path targetDir = Paths.get(getBaseDir(), targetSubDir);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        Path target = targetDir.resolve(source.getFileName().toString());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("本地文件移动: {} -> {}", storedPath, target);
        return target.toString();
    }

    @Override
    public void delete(String storedPath) throws IOException {
        if (storedPath == null) return;
        Path path = Paths.get(storedPath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("本地文件删除: {}", storedPath);
        }
    }

    @Override
    public InputStream openStream(String storedPath) throws IOException {
        Path path = Paths.get(storedPath);
        return Files.newInputStream(path);
    }

    @Override
    public String getAccessUrl(String storedPath) {
        if (storedPath == null) return null;
        // 将文件系统路径转换为 /upload/xxx/... 的公开 URL
        String baseDir = getBaseDir();
        String normalizedBase = baseDir.replace("\\", "/");
        if (!normalizedBase.endsWith("/")) normalizedBase = normalizedBase + "/";
        String normalizedPath = storedPath.replace("\\", "/");
        if (normalizedPath.startsWith(normalizedBase)) {
            return "/upload/" + normalizedPath.substring(normalizedBase.length());
        }
        return "/upload/" + normalizedPath;
    }

    @Override
    public String getType() {
        return "LOCAL";
    }
}
