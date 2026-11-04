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
	@Schema(description = "ID")
	private Long id;
	@Schema(description = "业务编码")
	private String number;
	@Schema(description = "名称")
	private String name;
	@Schema(description = "所属领域ID")
	private Long domainId;
	@Schema(description = "所属领域名称")
	private String domainName;
	@Schema(description = "所属应用ID")
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
