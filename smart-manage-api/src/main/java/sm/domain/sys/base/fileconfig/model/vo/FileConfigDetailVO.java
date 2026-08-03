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
}
