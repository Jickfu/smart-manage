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

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String sqlText;
    private Integer executeDuration;
    private String resultType;
    private Integer rowCount;
    private String errorMessage;
    private String createName;
    private String createIp;
    private String remark;
}
