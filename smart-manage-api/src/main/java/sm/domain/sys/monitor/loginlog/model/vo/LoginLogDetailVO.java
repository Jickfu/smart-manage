package sm.domain.sys.monitor.loginlog.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "登录日志详情")
public class LoginLogDetailVO implements Serializable {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String eventType;
    private Boolean success;
    private String failReason;
    private String ip;
    private String userAgent;
    private String traceId;
    private LocalDateTime createTime;
}
