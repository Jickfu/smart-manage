package sm.domain.sys.base.weakpassword.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WeakPasswordVO {
    private Long id;
    private Integer version;
    private String word;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
