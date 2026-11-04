package sm.system.response;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 分页数据载荷，必须依托 Result 作为接口响应外壳。
 *
 * @author Chekfu
 */
@Schema(description = "分页数据")
@Getter
public final class PageData<T> {
    @Schema(description = "当前页码，从 1 开始")
    private final long pageNum;
    @Schema(description = "每页记录数")
    private final long pageSize;
    @Schema(description = "记录总数")
    private final long total;
    @Schema(description = "查询结果记录列表")
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
