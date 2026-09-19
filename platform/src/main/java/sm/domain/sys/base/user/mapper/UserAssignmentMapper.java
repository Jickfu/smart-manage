package sm.domain.sys.base.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.user.model.entity.UserAssignmentEntity;

@Mapper
public interface UserAssignmentMapper extends BaseMapper<UserAssignmentEntity> {
}
