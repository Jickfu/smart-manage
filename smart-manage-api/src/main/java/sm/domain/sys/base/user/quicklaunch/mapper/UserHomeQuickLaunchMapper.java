package sm.domain.sys.base.user.quicklaunch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.user.quicklaunch.model.entity.UserHomeQuickLaunchEntity;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;
import sm.domain.sys.base.user.quicklaunch.model.vo.QuickLaunchItemVO;

import java.util.List;

@Mapper
public interface UserHomeQuickLaunchMapper extends BaseMapper<UserHomeQuickLaunchEntity> {
    List<QuickLaunchItemVO> selectCurrentUserItems(
            @Param("userId") Long userId,
            @Param("orgId") Long orgId,
            @Param("administrator") boolean administrator,
            @Param("scope") HomeScopeEnum scope,
            @Param("appId") Long appId);

    List<Long> selectMenuIds(
            @Param("userId") Long userId,
            @Param("scope") HomeScopeEnum scope,
            @Param("appId") Long appId);

    int deleteScope(
            @Param("userId") Long userId,
            @Param("scope") HomeScopeEnum scope,
            @Param("appId") Long appId);

    Long lockUser(@Param("userId") Long userId);
}
