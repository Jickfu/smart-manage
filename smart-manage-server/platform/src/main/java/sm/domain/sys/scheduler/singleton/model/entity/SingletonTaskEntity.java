package sm.domain.sys.scheduler.singleton.model.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
/** 集群单例任务的共享控制与运行状态。 */
@Data
@TableName("t_sys_singleton_task")
public class SingletonTaskEntity {
    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 任务编码
     */
    private String number;
    /**
     * 任务名称
     */
    private String name;
    /**
     * 所属应用编码
     */
    private String appNumber;
    /**
     * 任务说明
     */
    private String description;
    /**
     * 是否允许管理端启停
     */
    private Boolean controllable;
    /**
     * 期望是否运行
     */
    private Boolean requested;
    /**
     * 启停命令乐观锁版本
     */
    @Version
    private Integer version;
    /**
     * 运行状态：STOPPED、STARTING、RUNNING、STOPPING、FAILED、UNKNOWN
     */
    private String state;
    /**
     * 当前执行实例
     */
    private String ownerInstanceId;
    /**
     * 本次运行身份
     */
    private String runId;
    /**
     * 单调递增的执行代际
     */
    private Long generation;
    /**
     * 本次开始时间
     */
    private java.time.LocalDateTime startedAt;
    /**
     * 最近心跳时间
     */
    private java.time.LocalDateTime heartbeatAt;
    /**
     * 失败退避截止时间
     */
    private java.time.LocalDateTime retryAt;
    /**
     * 安全的失败分类
     */
    private String lastError;
}
