package sm.domain.sys.base.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

import java.time.LocalDateTime;

/**
 * 权限详情 VO
 *
 * @author Chekfu
 */
@Data
@Schema(description = "权限详情")
public class PermissionDetailVO {

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "乐观锁版本号")
	private Integer version;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "编码")
	private String number;

	private ReferenceVO feature;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	@Schema(description = "创建人")
	private Long createUser;

	@Schema(description = "修改人")
	private Long updateUser;
}
