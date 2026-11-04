package sm.domain.sys.base.basicdata.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import sm.domain.sys.base.common.model.vo.ReferenceVO;

@Schema(description = "基础资料详情输出模型")
@Data
public class BasicDataItemDetailVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "基础资料分类信息")
    private ReferenceVO category;
    @Schema(description = "上级基础资料引用")
    private ReferenceVO parent;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "排序号")
    private Integer sort;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "是否系统预置")
    private Boolean systemPreset;
    @Schema(description = "级别")
    private Integer level;
    @Schema(description = "长编码")
    private String numberPath;
    @Schema(description = "长名称")
    private String namePath;
    @Schema(description = "是否叶子节点")
    private Boolean isLeaf;
    @Schema(description = "乐观锁版本号")
    private Integer version;
}
