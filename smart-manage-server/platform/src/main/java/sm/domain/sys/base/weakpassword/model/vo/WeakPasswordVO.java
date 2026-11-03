package sm.domain.sys.base.weakpassword.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "弱密码输出模型")
@Data
public class WeakPasswordVO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "乐观锁版本号")
    private Integer version;
    @Schema(description = "完整弱口令词条")
    private String word;
    @Schema(description = "说明")
    private String description;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
