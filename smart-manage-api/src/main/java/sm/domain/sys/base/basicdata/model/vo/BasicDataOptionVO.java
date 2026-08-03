package sm.domain.sys.base.basicdata.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "基础数据选项")
public class BasicDataOptionVO {

    private Long id;

    private Long parentId;

    @Schema(description = "编码")
    private String number;

    @Schema(description = "名称")
    private String name;

    private String namePath;

    private Boolean isLeaf;

    public BasicDataOptionVO(Long id, Long parentId, String number, String name, String namePath, Boolean isLeaf) {
        this.id = id;
        this.parentId = parentId;
        this.number = number;
        this.name = name;
        this.namePath = namePath;
        this.isLeaf = isLeaf;
    }
}
