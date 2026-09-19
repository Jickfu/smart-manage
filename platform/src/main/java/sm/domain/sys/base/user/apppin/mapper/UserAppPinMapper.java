package sm.domain.sys.base.user.apppin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.user.apppin.model.entity.UserAppPinEntity;
import sm.domain.sys.base.user.apppin.model.vo.PinnedAppVO;

import java.util.List;

@Mapper
public interface UserAppPinMapper extends BaseMapper<UserAppPinEntity> {
	List<PinnedAppVO> selectUserPins(@Param("userId") Long userId, @Param("orgId") Long orgId,
			@Param("administrator") boolean administrator);

	Integer selectNextSeq(@Param("userId") Long userId);

	Long lockUser(@Param("userId") Long userId);

	int deleteByUserAndAppNumber(@Param("userId") Long userId, @Param("appNumber") String appNumber);
}
