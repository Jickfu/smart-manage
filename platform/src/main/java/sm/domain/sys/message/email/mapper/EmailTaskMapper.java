package sm.domain.sys.message.email.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import sm.domain.sys.message.email.model.entity.EmailTaskEntity;
import java.time.LocalDateTime;
import java.util.List;
@Mapper public interface EmailTaskMapper extends BaseMapper<EmailTaskEntity> {
    @Select("""
        WITH candidates AS (
          SELECT id FROM t_sys_email_task
          WHERE status IN ('PENDING','RETRY_WAIT') AND (next_attempt_time IS NULL OR next_attempt_time <= #{now})
          ORDER BY create_time, id FOR UPDATE SKIP LOCKED LIMIT #{limit}
        )
        UPDATE t_sys_email_task t SET status='SENDING', started_time=#{now}, update_time=#{now}, version=version+1
        FROM candidates c WHERE t.id=c.id RETURNING t.*
        """)
    List<EmailTaskEntity> claim(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
