package sm.domain.sys.base.menu.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 菜单树 VO — 用于前端左树右表全量获取应用下所有菜单。
 * 包含 parentId 以便前端构建树形结构。
 *
 * @author Chekfu
 */
@Data
@Schema(description = "菜单树视图")
public class MenuTreeVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "编号")
    private String number;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "菜单层级：2=分组，3=页面")
    private Integer level;

    @Schema(description = "父菜单ID，一级菜单为0")
    private Long parentId;

    @Schema(description = "所属应用ID")
    private Long appId;

    @Schema(description = "所属应用名称")
    private String appName;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "子菜单；仅分组节点包含页面子节点")
    private List<MenuTreeVO> children;
}
