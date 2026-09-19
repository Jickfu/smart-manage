package sm.domain.sys.base.openapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sm.domain.sys.base.openapi.model.entity.OpenApiInvocationLogEntity;

@Mapper
public interface OpenApiInvocationLogMapper extends BaseMapper<OpenApiInvocationLogEntity> {
    @Select("""
            SELECT count(*) AS total_count,
                   count(*) FILTER (WHERE result_type = 'SUCCESS') AS success_count,
                   coalesce(round(avg(duration_ms)), 0) AS average_duration_ms,
                   count(DISTINCT application_id) AS application_count
            FROM t_sys_openapi_invocation_log
            WHERE request_time >= now() - interval '24 hours'
            """)
    java.util.Map<String, Object> selectLast24HoursSummary();

    @Select("""
            SELECT operation_key, count(*) AS call_count,
                   count(*) FILTER (WHERE result_type = 'SUCCESS') AS success_count,
                   coalesce(round(avg(duration_ms)), 0) AS average_duration_ms
            FROM t_sys_openapi_invocation_log
            WHERE request_time >= now() - interval '24 hours'
            GROUP BY operation_key ORDER BY call_count DESC, operation_key
            """)
    java.util.List<java.util.Map<String, Object>> selectLast24HoursByOperation();
}
