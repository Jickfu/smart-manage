package sm.domain.sys.base.basicdata.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Schema(description = "基础资料树节点输出模型")
@Data
@AllArgsConstructor
public class BasicDataTreeVO {
    @Schema(description = "基础资料树节点键")
    private String key;
    @Schema(description = "基础资料树节点类型")
    private String type;
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "是否启用")
    private Boolean enabled;
    @Schema(description = "子节点列表")
    private List<BasicDataTreeVO> children;
}
