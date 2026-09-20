package sm.domain.sys.base.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.vo.MenuAppInfoVO;

import java.util.List;

/**
 * @author Chekfu
 */
@Mapper
public interface MenuMapper extends BaseMapper<MenuEntity> {
    MenuAppInfoVO selectAppInfo(Long appId);

    List<MenuEntity> selectUserMenus(@Param("userId") Long userId,
                                     @Param("orgId") Long orgId,
                                     @Param("appId") Long appId,
                                     @Param("admin") boolean admin);

    int updateAllColumns(MenuEntity entity);
}
