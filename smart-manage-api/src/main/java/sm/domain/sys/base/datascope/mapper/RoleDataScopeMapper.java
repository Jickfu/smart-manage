package sm.domain.sys.base.datascope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.datascope.model.entity.RoleDataScopeEntity;
import java.util.List;

@Mapper
public interface RoleDataScopeMapper extends BaseMapper<RoleDataScopeEntity> {
    List<RoleDataScopeEntity> selectEffectiveRules(@Param("userId") Long userId,
                                                    @Param("orgId") Long orgId,
                                                    @Param("resourceType") String resourceType,
                                                    @Param("action") String action);
}
