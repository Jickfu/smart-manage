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

    /** 存储由服务端生成的文件；目录必须由受控用途生成，调用方不得传入物理路径。 */
    FileStoreResult store(String subDir, String originalName, String contentType,
                          long fileSize, InputStream inputStream) throws IOException;

    /** 删除文件 */
    void delete(String storedPath) throws IOException;

    /** 打开下载流；调用方必须关闭返回的流。 */
    InputStream openStream(String storedPath) throws IOException;

    /**
     * 生成已经完成业务授权后的短时直连地址。
     *
     * <p>仅私有对象存储可以实现；Local 和 FTP 返回 {@code null}，由受控下载接口代理文件流。</p>
     */
    default String createAuthorizedDownloadUrl(String storedPath) {
        return null;
    }

    /** 获取存储类型标识 */
    String getType();
}
