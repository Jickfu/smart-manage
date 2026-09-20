package sm.domain.sys.base.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 引用选择器详情回显所需的最小实体信息。 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceVO {
    private Long id;
    private String number;
    private String name;
}
