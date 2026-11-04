package sm.domain.sys.base.org.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Schema(description = "组织选项输出模型")
@Data
public class OrgOptionVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "业务编码")
    private String number;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "父ID")
    private Long parentId;
    @Schema(description = "长名称")
    private String namePath;
}

