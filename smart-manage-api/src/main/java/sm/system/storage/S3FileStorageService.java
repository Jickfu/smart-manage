package sm.system.storage;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;
import java.util.HexFormat;

/** AWS S3 与 MinIO 共用的私有对象存储实现。 */
@Component
public class S3FileStorageService implements FileStorageService {
    private final FileStorageConfigProvider configProvider;
    private volatile CachedClients cachedClients;

    public S3FileStorageService(FileStorageConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    @Override
    public FileStoreResult store(String subDir, MultipartFile file) throws IOException {
        FileStorageConfig config = config();
        String objectKey = objectKey(subDir, file.getOriginalFilename());
        try {
            S3Client client = clients(config).client();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(config.s3Bucket())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();
            client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return FileStoreResult.of(objectKey.substring(objectKey.lastIndexOf('/') + 1), objectKey, file.getSize());
        } catch (RuntimeException exception) {
            throw new IOException("S3 对象上传失败", exception);
        }
    }

    @Override
    public void delete(String storedPath) throws IOException {
        FileStorageConfig config = config();
        try {
            S3Client client = clients(config).client();
            client.deleteObject(DeleteObjectRequest.builder().bucket(config.s3Bucket()).key(storedPath).build());
        } catch (RuntimeException exception) {
            throw new IOException("S3 对象删除失败", exception);
        }
    }

    @Override
    public InputStream openStream(String storedPath) throws IOException {
        FileStorageConfig config = config();
        try {
            return clients(config).client().getObject(GetObjectRequest.builder()
                    .bucket(config.s3Bucket()).key(storedPath).build());
        } catch (RuntimeException exception) {
            throw new IOException("S3 对象下载失败", exception);
        }
    }

    @Override
    public String createAuthorizedDownloadUrl(String storedPath) {
        FileStorageConfig config = config();
        S3Presigner presigner = clients(config).presigner();
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(config.s3Bucket())
                .key(storedPath)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(1))
                .getObjectRequest(objectRequest)
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public String getType() {
        return "S3";
    }

    private FileStorageConfig config() {
        return configProvider.getFileStorageConfig();
    }

    private synchronized S3Clients clients(FileStorageConfig config) {
        String fingerprint = configurationFingerprint(config);
        if (cachedClients != null && cachedClients.fingerprint().equals(fingerprint)) {
            return cachedClients.clients();
        }
        S3Clients nextClients = new S3Clients(client(config), presigner(config));
        CachedClients previousClients = cachedClients;
        cachedClients = new CachedClients(fingerprint, nextClients);
        if (previousClients != null) {
            previousClients.clients().close();
        }
        return nextClients;
    }

    /** 配置变化时使用新客户端；摘要避免把存储凭据放入缓存键或诊断输出。 */
    private String configurationFingerprint(FileStorageConfig config) {
        String material = String.join("\u0000",
                value(config.s3Endpoint()), value(config.s3Region()), value(config.s3Bucket()),
                value(config.s3AccessKey()), value(config.s3SecretKey()), value(config.s3PathStyle()));
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("运行环境不支持 SHA-256", exception);
        }
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    @PreDestroy
    synchronized void closeClients() {
        if (cachedClients != null) {
            cachedClients.clients().close();
            cachedClients = null;
        }
    }

    private S3Client client(FileStorageConfig config) {
        return S3Client.builder()
                .endpointOverride(URI.create(config.s3Endpoint()))
                .region(Region.of(config.s3Region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.s3AccessKey(), config.s3SecretKey())))
                .forcePathStyle(Boolean.TRUE.equals(config.s3PathStyle()))
                .build();
    }

    private S3Presigner presigner(FileStorageConfig config) {
        return S3Presigner.builder()
                .endpointOverride(URI.create(config.s3Endpoint()))
                .region(Region.of(config.s3Region()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(config.s3AccessKey(), config.s3SecretKey())))
                .serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                        .pathStyleAccessEnabled(Boolean.TRUE.equals(config.s3PathStyle()))
                        .build())
                .build();
    }

    private record S3Clients(S3Client client, S3Presigner presigner) {
        private void close() {
            client.close();
            presigner.close();
        }
    }

    private record CachedClients(String fingerprint, S3Clients clients) {
    }

    String objectKey(String subDir, String originalName) {
        String extension = safeExtension(originalName);
        String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT);
        LocalDate date = LocalDate.now();
        String normalizedDir = subDir == null ? "other" : subDir.replace('\\', '/').replaceAll("^/+|/+$", "");
        if (normalizedDir.contains("..")) {
            throw new IllegalArgumentException("对象键目录不允许包含相对路径片段");
        }
        return normalizedDir + "/" + date.getYear() + "/" + String.format("%02d", date.getMonthValue())
                + "/" + uuid.substring(0, 2) + "/" + uuid + extension;
    }

    private String safeExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        String extension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        return extension.matches("[a-z0-9]{1,10}") ? "." + extension : "";
    }
}
