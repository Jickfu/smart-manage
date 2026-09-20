package sm.system.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文件存储结果
 *
 * @author Chekfu
 */
@Data
@AllArgsConstructor(staticName = "of")
public class FileStoreResult {

    /** 存储后文件名（UUID） */
    private String storedName;

    /** 存储路径 */
    private String storedPath;

    /** 文件大小（字节） */
    private long fileSize;
}
