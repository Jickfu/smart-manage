package sm.domain.sys.monitor.sql.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * SQL 执行日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_sql_log")
public class SqlLogEntity extends BaseEntity {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * SQL语句
     */
    private String sqlText;
    /**
     * 执行耗时(ms)
     */
    private Integer executeDuration;
    /**
     * SQL 执行结果类型：QUERY、DML、DDL、ERROR
     */
    private String resultType;
    /**
     * 影响行数
     */
    private Integer rowCount;
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
     * 备注
     */
    private String remark;
}
