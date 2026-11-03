package sm.domain.sys.base.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 引用选择器详情回显所需的最小实体信息。
 */
@Schema(description = "基础资料引用输出模型")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "名称")
    private String name;
}
