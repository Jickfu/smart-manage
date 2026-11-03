package sm.domain.sys.monitor.script.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(description = "脚本详情输出模型")
@Data
@EqualsAndHashCode(callSuper = true)
public class ScriptDetailVO extends ScriptListVO {
    @Schema(description = "JavaScript 脚本正文")
    private String content;
    @Schema(description = "乐观锁版本号")
    private Integer version;
}
