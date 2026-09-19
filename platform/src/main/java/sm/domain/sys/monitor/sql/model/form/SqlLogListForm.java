package sm.domain.sys.monitor.sql.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.form.PageForm;

import java.time.LocalDateTime;

/**
 * SQL 日志分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "SQL 日志分页查询")
public class SqlLogListForm extends PageForm {

    @Schema(description = "关键字搜索（SQL 文本 LIKE）")
    private String keyword;

    @Schema(description = "结果类型过滤：QUERY/DML/DDL/ERROR")
    private String resultType;

    @Schema(description = "执行开始时间")
    private LocalDateTime startTime;

    @Schema(description = "执行结束时间")
    private LocalDateTime endTime;
}
