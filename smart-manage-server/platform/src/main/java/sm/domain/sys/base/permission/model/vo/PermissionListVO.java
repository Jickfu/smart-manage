package sm.domain.sys.base.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Chekfu
 */
@Data
@Schema(description = "权限列表视图")
public class PermissionListVO {
	@Schema(description = "ID")
	private Long id;

	@Schema(description = "权限名称")
	private String name;

	@Schema(description = "权限编码")
	private String number;

	@Schema(description = "应用ID")
	private Long appId;

	@Schema(description = "所属应用名称")
	private String appName;

	@Schema(description = "所属功能ID")
	private Long featureId;

	@Schema(description = "稳定功能键")
	private String featureKey;

	@Schema(description = "所属功能名称")
	private String featureName;
}
