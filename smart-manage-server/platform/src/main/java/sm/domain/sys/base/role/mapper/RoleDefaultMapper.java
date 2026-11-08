package sm.domain.sys.base.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.role.model.entity.RoleDefaultEntity;
import sm.domain.sys.base.role.model.vo.RoleDefaultVO;

/** 默认角色单例配置持久化。 */
@Mapper
public interface RoleDefaultMapper extends BaseMapper<RoleDefaultEntity> {
    RoleDefaultVO selectConfiguration();
    RoleDefaultEntity selectForUpdate();
    int updateRole(@Param("roleId") Long roleId, @Param("version") Integer version);
}
