package sm.system.response;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 分页数据载荷，必须依托 Result 作为接口响应外壳。
 *
 * @author Chekfu
 */
@Getter
public final class PageData<T> {
    private final long pageNum;
    private final long pageSize;
    private final long total;
    private final List<T> records;

    private PageData(long total, long pageNum, long pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        // 前端分页表格统一按数组处理，空结果固定返回 []，不返回 null。
        this.records = records == null ? Collections.emptyList() : records;
    }

    public static <T> PageData<T> of(long total, long pageNum, long pageSize, List<T> records) {
        return new PageData<>(total, pageNum, pageSize, records);
    }
}
