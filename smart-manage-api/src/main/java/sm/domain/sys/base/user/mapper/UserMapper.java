package sm.domain.sys.base.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import sm.system.query.ListSqlQuery;
import sm.domain.sys.base.user.model.vo.UserAssignedRoleVO;
import sm.domain.sys.base.user.model.UserCacheSnapshot;
import sm.domain.sys.base.user.model.UserCredentialSnapshot;

/**
 * @author Chekfu
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	UserEntity selectSecurityState(@Param("id") Long id);
	int updatePasswordByVerifiedEmail(@Param("snapshot") UserCredentialSnapshot snapshot, @Param("password") String password);
	int bindVerifiedEmail(@Param("snapshot") UserCredentialSnapshot snapshot, @Param("email") String email);
	int changeResetPassword(@Param("id") Long id, @Param("generation") Long generation, @Param("password") String password);
	Page<UserEntity> selectScopedPage(Page<UserEntity> page,
			@Param("keyword") String keyword,
			@Param("orgIds") List<Long> orgIds,
			@Param("unassigned") boolean unassigned,
			@Param("ids") List<Long> ids,
			@Param("listQuery") ListSqlQuery listQuery);

	List<UserAssignedRoleVO> selectAssignedRoles(@Param("userId") Long userId);

	UserCacheSnapshot selectCacheSnapshotById(@Param("id") Long id);
}
