package sm.system.storage;

/** 文件存储运行配置，仅在服务端内部传递。 */
public record FileStorageConfig(
        String storageType,
        String localDir,
        String ftpHost,
        Integer ftpPort,
        String ftpUsername,
        String ftpPassword,
        String ftpDir,
        Boolean ftpPassiveMode,
        String s3Endpoint,
        String s3Region,
        String s3Bucket,
        String s3AccessKey,
        String s3SecretKey,
        Boolean s3PathStyle) {
}
