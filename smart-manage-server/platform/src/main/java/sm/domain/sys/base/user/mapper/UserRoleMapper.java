package sm.domain.sys.base.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.user.model.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Collection;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {
    List<UserRoleEntity> selectByPermissionId(@Param("permissionId") Long permissionId);
    List<Long> selectOrgIdsByUserId(@Param("userId") Long userId);
    List<Long> selectExistingRoleIds(@Param("roleIds") Collection<Long> roleIds);
}
