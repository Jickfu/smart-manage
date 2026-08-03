package sm.domain.sys.base.basicdata.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BasicDataTreeVO {
    private String key;
    private String type;
    private Long id;
    private String name;
    private Boolean enabled;
    private List<BasicDataTreeVO> children;
}
