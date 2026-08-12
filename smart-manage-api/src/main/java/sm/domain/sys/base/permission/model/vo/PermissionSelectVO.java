package sm.domain.sys.base.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限-基础资料选择-列表项。
 *
 * @author Chekfu
 */
@Data
@Schema(description = "权限-基础资料选择-列表项")
public class PermissionSelectVO {
	private Long id;
	private String number;
	private String name;
	private Long appId;
	private String appName;
	private Long featureId;
	private String featureKey;
	private String featureName;
}
