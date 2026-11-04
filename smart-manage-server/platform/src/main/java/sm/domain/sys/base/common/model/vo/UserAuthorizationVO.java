package sm.domain.sys.base.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户在指定组织下的共享授权快照。
 */
@Schema(description = "用户授权信息输出模型")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorizationVO implements Serializable {
    @Schema(description = "角色编码集合")
    private List<String> roleNumbers;
    @Schema(description = "权限码集合")
    private List<String> permissionNumbers;
}
