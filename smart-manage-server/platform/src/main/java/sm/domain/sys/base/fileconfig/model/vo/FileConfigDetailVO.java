package sm.domain.sys.base.fileconfig.model.vo;

import lombok.Data;

/**
 * 文件配置详情 VO
 *
 * @author Chekfu
 */
@Data
public class FileConfigDetailVO {

    private Long id;

    private Integer version;

    private String storageType;

    private String localDir;

    private String ftpHost;

    private Integer ftpPort;

    private String ftpUsername;

    private Boolean ftpPasswordConfigured;

    private String ftpDir;

    private Boolean ftpPassiveMode;

    private String s3Endpoint;
    private String s3Region;
    private String s3Bucket;
    private String s3AccessKey;
    private Boolean s3SecretKeyConfigured;
    private Boolean s3PathStyle;
}
