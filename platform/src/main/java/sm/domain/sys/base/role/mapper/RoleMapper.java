package sm.domain.sys.base.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.role.model.entity.RoleEntity;

/**
 * @author Chekfu
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {

    /** 锁定聚合根，即使权限集合为空也必须串行替换。 */
    RoleEntity selectForUpdate(@Param("id") Long id);

    java.util.List<String> selectUserRoleNumbers(
            @Param("userId") Long userId,
            @Param("orgId") Long orgId);
}
