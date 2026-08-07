package sm.system.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 文件存储服务接口
 *
 * @author Chekfu
 */
public interface FileStorageService {

    /** 存储文件到指定子目录（如 sys、biz/expense_report、other） */
    FileStoreResult store(String subDir, MultipartFile file) throws IOException;

    /** 存储临时文件（写入 temp 子目录） */
    FileStoreResult storeTemp(MultipartFile file) throws IOException;

    /** 将文件移动到目标子目录，调用方可使用同一能力执行失败补偿。 */
    String move(String storedPath, String targetSubDir) throws IOException;

    /** 将临时文件提升到目标子目录。 */
    default String promote(String tempPath, String targetSubDir) throws IOException {
        return move(tempPath, targetSubDir);
    }

    /** 删除文件 */
    void delete(String storedPath) throws IOException;

    /** 打开下载流；调用方必须关闭返回的流。 */
    InputStream openStream(String storedPath) throws IOException;

    /** 获取文件公开访问 URL */
    String getAccessUrl(String storedPath);

    /** 获取存储类型标识 */
    String getType();
}
