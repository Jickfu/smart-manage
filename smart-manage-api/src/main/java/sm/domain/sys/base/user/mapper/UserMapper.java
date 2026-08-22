package sm.domain.sys.base.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import sm.system.query.ListSqlQuery;
import sm.domain.sys.base.user.model.vo.UserAssignedRoleVO;

/**
 * @author Chekfu
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	Page<UserEntity> selectScopedPage(Page<UserEntity> page,
			@Param("keyword") String keyword,
			@Param("orgIds") List<Long> orgIds,
			@Param("unassigned") boolean unassigned,
			@Param("listQuery") ListSqlQuery listQuery);

	List<UserAssignedRoleVO> selectAssignedRoles(@Param("userId") Long userId);
}
