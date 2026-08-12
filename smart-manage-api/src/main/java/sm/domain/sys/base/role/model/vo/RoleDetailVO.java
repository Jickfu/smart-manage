package sm.domain.sys.base.role.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情 VO
 *
 * @author Chekfu
 */
@Data
@Schema(description = "角色详情")
public class RoleDetailVO {

	@Schema(description = "ID")
	private Long id;

	@Schema(description = "名称")
	private String name;

	@Schema(description = "编码")
	private String number;

	@Schema(description = "描述")
	private String description;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@Schema(description = "更新时间")
	private LocalDateTime updateTime;

	@Schema(description = "创建人")
	private Long createUser;

	@Schema(description = "修改人")
	private Long updateUser;

	private Integer version;

	@Schema(description = "权限ID列表")
	private List<Long> permissionIds;
}
