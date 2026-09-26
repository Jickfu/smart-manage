package sm.domain.workflow.process.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import sm.domain.workflow.process.notification.model.entity.WorkflowOutboxEntity;
import java.util.List;
import java.util.UUID;

@Mapper
public interface WorkflowOutboxMapper extends BaseMapper<WorkflowOutboxEntity> {
    @Insert("""
        INSERT INTO t_workflow_outbox(id,event_key,instance_id,recipient_id,title,content)
        VALUES(#{row.id},#{row.eventKey},#{row.instanceId},#{row.recipientId},#{row.title},#{row.content})
        ON CONFLICT(event_key) DO NOTHING
        """)
    int enqueue(@Param("row") WorkflowOutboxEntity row);

    @Select("""
        UPDATE t_workflow_outbox SET claim_token=#{token}, claimed_until=CURRENT_TIMESTAMP+INTERVAL '2 minutes',
          attempts=attempts+1, update_time=CURRENT_TIMESTAMP
        WHERE id IN (SELECT id FROM t_workflow_outbox WHERE status='PENDING'
          AND next_attempt_at<=CURRENT_TIMESTAMP AND (claimed_until IS NULL OR claimed_until<CURRENT_TIMESTAMP)
          ORDER BY next_attempt_at,id LIMIT 20 FOR UPDATE SKIP LOCKED)
        RETURNING id,event_key,instance_id,recipient_id,title,content,claim_token
        """)
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    List<WorkflowOutboxEntity> claim(@Param("token") UUID token);

    @Update("""
        UPDATE t_workflow_outbox SET status='DELIVERED',claim_token=NULL,claimed_until=NULL,last_error=NULL,
          update_time=CURRENT_TIMESTAMP WHERE id=#{id} AND claim_token=#{token} AND status='PENDING'
        """)
    int delivered(@Param("id") Long id, @Param("token") UUID token);

    @Update("""
        UPDATE t_workflow_outbox SET claim_token=NULL,claimed_until=NULL,last_error=#{reason},
          next_attempt_at=CURRENT_TIMESTAMP+INTERVAL '1 minute'*LEAST(attempts,60),update_time=CURRENT_TIMESTAMP
        WHERE id=#{id} AND claim_token=#{token} AND status='PENDING'
        """)
    int retry(@Param("id") Long id, @Param("token") UUID token, @Param("reason") String reason);
}
