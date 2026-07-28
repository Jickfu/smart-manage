package sm.domain.sys.base.cloud.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 云详情 VO
 *
 * @author Chekfu
 */
@Data
@Schema(description = "云详情")
public class CloudDetailVO {

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "乐观锁版本号")
	private Integer version;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "编码")
	private String number;

	@Schema(description = "排序")
	private Integer seq;

	@Schema(description = "是否启用")
	private Boolean enabled;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	@Schema(description = "创建人")
	private Long createUser;

	@Schema(description = "修改人")
	private Long updateUser;
}
