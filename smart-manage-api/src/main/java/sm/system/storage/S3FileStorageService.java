package sm.system.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

/** AWS S3 与 MinIO 共用的私有对象存储实现。 */
@Component
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {
    private final FileStorageConfigProvider configProvider;

    @Override
    public FileStoreResult store(String subDir, MultipartFile file) throws IOException {
        FileStorageConfig config = config();
        String objectKey = objectKey(subDir, file.getOriginalFilename());
        try (S3Client client = client(config)) {
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
    public FileStoreResult storeTemp(MultipartFile file) throws IOException {
        return store("temp", file);
    }

    @Override
    public String move(String storedPath, String targetSubDir) throws IOException {
        FileStorageConfig config = config();
        String targetKey = targetSubDir + "/" + storedPath.substring(storedPath.lastIndexOf('/') + 1);
        try (S3Client client = client(config)) {
            client.copyObject(CopyObjectRequest.builder().sourceBucket(config.s3Bucket()).sourceKey(storedPath)
                    .destinationBucket(config.s3Bucket()).destinationKey(targetKey).build());
            client.deleteObject(DeleteObjectRequest.builder().bucket(config.s3Bucket()).key(storedPath).build());
            return targetKey;
        } catch (RuntimeException exception) {
            throw new IOException("S3 对象移动失败", exception);
        }
    }

    @Override
    public void delete(String storedPath) throws IOException {
        FileStorageConfig config = config();
        try (S3Client client = client(config)) {
            client.deleteObject(DeleteObjectRequest.builder().bucket(config.s3Bucket()).key(storedPath).build());
        } catch (RuntimeException exception) {
            throw new IOException("S3 对象删除失败", exception);
        }
    }

    @Override
    public byte[] getBytes(String storedPath) throws IOException {
        FileStorageConfig config = config();
        try (S3Client client = client(config)) {
            return client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(config.s3Bucket()).key(storedPath).build()).asByteArray();
        } catch (RuntimeException exception) {
            throw new IOException("S3 对象下载失败", exception);
        }
    }

    @Override
    public String getAccessUrl(String storedPath) {
        FileStorageConfig config = config();
        try (S3Presigner presigner = presigner(config)) {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(config.s3Bucket())
                    .key(storedPath)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(objectRequest)
                    .build();
            return presigner.presignGetObject(presignRequest).url().toString();
        }
    }

    @Override
    public String getType() {
        return "S3";
    }

    private FileStorageConfig config() {
        return configProvider.getFileStorageConfig();
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
