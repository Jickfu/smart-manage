package sm.domain.sys.monitor.arthas.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Arthas 命令执行结果
 */
@Data
@Schema(description = "Arthas 命令执行结果")
public class ArthasResultVO {

    @Schema(description = "是否执行成功")
    private boolean success;

    @Schema(description = "命令输出文本（已去除 ANSI 转义）")
    private String output;

    @Schema(description = "持续命令的会话 ID，一次性命令为 null")
    private String sessionId;

    @Schema(description = "会话所在应用实例 ID")
    private String instanceId;

    public ArthasResultVO withInstanceId(String value) {
        setInstanceId(value);
        return this;
    }

    public static ArthasResultVO ok(String output) {
        ArthasResultVO vo = new ArthasResultVO();
        vo.setSuccess(true);
        vo.setOutput(output);
        return vo;
    }

    public static ArthasResultVO fail(String output) {
        ArthasResultVO vo = new ArthasResultVO();
        vo.setSuccess(false);
        vo.setOutput(output);
        return vo;
    }

    public static ArthasResultVO running(String sessionId, String output) {
        ArthasResultVO vo = new ArthasResultVO();
        vo.setSuccess(true);
        vo.setOutput(output);
        vo.setSessionId(sessionId);
        return vo;
    }
}
