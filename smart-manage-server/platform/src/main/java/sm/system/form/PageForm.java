package sm.system.form;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 基础分页参数类
 *
 * @author Chekfu
 */
@Schema(description = "分页查询输入表单")
@Data
public class PageForm {
	// 当前页码，默认第一页
	@Schema(description = "当前页码，从 1 开始；为空或小于 1 时按 1 处理")
	private Integer pageNum = 1;
	// 每页数量，默认10条
	@Schema(description = "每页记录数，默认 10，最大 100；为空或小于 1 时按 10 处理")
	private Integer pageSize = 10;
	// 列表表头筛选条件使用受控 JSON 传输，具体字段仍由各领域 Service 白名单注册。
	@Schema(description = "受控列表筛选 JSON；允许的字段和操作符由所属 Service 注册")
	private String filters;
	// 排序字段只能使用各领域 Service 注册的稳定字段键。
	@Schema(description = "排序字段，仅允许所属 Service 注册的稳定字段键")
	private String sortField;
	@Schema(description = "排序方向：ASC 升序、DESC 降序，不区分大小写")
	private String sortOrder;

	public Integer getPageNum() {
		return pageNum == null || pageNum < 1 ? 1 : pageNum;
	}

	public Integer getPageSize() {
		if (pageSize == null || pageSize < 1) {
			return 10;
		}
		// 限制最大100条
		return pageSize > 100 ? 100 : pageSize;
	}
}
