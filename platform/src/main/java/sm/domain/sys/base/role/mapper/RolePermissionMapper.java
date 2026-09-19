package sm.domain.sys.base.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.role.model.entity.RolePermissionEntity;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermissionEntity> {
}
