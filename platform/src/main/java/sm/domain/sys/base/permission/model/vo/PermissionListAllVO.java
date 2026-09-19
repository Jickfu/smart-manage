package sm.domain.sys.base.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 权限全量查询 VO — 不经过分页，直接返回所有启用权限。
 *
 * @author Chekfu
 */
@Data
@Schema(description = "权限全量查询视图")
public class PermissionListAllVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "权限名称")
    private String name;

    @Schema(description = "权限编码")
    private String number;

    @Schema(description = "所属应用ID")
    private Long appId;
}
