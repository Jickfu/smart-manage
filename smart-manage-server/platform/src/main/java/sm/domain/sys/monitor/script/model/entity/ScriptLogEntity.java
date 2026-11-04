package sm.domain.sys.monitor.script.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 脚本执行日志持久化实体。
 */
@Data
@TableName("t_sys_script_log")
public class ScriptLogEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 关联脚本ID
     */
    private Long scriptId;
    /**
     * 脚本名称快照
     */
    private String scriptName;
    /**
     * 实际执行脚本快照
     */
    private String scriptContent;
    /**
     * 脚本事务模式：ATOMIC 原子事务、NON_ATOMIC 非原子执行
     */
    private String transactionMode;
    /**
     * 脚本执行状态：SUCCESS 成功、ERROR 错误、TIMEOUT 超时
     */
    private String executeStatus;
    /**
     * 执行耗时(ms)
     */
    private Integer executeDuration;
    /**
     * 事务结果：COMMITTED 已提交、ROLLED_BACK 已回滚、NOT_APPLICABLE 不适用
     */
    private String transactionResult;
    /**
     * 截断后的执行输出
     */
    private String output;
    /**
     * 错误说明
     */
    private String errorMessage;
    /**
     * 执行人
     */
    private String createName;
    /**
     * 执行IP
     */
    private String createIp;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
