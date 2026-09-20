package sm.domain.sys.base.openapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import sm.domain.sys.base.openapi.model.entity.OpenApiReleaseEntity;

@Mapper
public interface OpenApiReleaseMapper extends BaseMapper<OpenApiReleaseEntity> {
    @Update("""
            UPDATE t_sys_openapi_release
            SET status = #{status}, version = version + 1, update_time = now(), update_user = #{updateUser}
            WHERE id = #{id} AND version = #{version}
            """)
    int updateStatus(@Param("id") Long id, @Param("version") Integer version,
                     @Param("status") String status, @Param("updateUser") Long updateUser);
}
