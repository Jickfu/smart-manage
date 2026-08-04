package sm.domain.sys.monitor.script.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_sys_script_log")
public class ScriptLogEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long scriptId;
    private String scriptName;
    private String scriptContent;
    private String transactionMode;
    private String executeStatus;
    private Integer executeDuration;
    private String transactionResult;
    private String output;
    private String errorMessage;
    private String createName;
    private String createIp;
    private LocalDateTime createTime;
}
