package sm.domain.sys.base.basicdata.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class BasicDataListVO extends BasicDataItemDetailVO {
    private LocalDateTime updateTime;
}
