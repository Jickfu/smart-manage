package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** 菜单管理范围树节点：领域 → 应用 → 功能。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "菜单管理范围树节点")
public class MenuCatalogNodeVO {
    @Schema(description = "节点类型：DOMAIN、APPLICATION、FEATURE")
    private String type;
    @Schema(description = "节点ID")
    private Long id;
    @Schema(description = "稳定编码")
    private String number;
    @Schema(description = "显示名称")
    private String name;
    @Schema(description = "子节点")
    private List<MenuCatalogNodeVO> children = new ArrayList<>();
}
