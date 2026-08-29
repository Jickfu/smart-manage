package sm.domain.sys.base.user.quicklaunch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sm.domain.sys.base.user.quicklaunch.mapper.UserHomeQuickLaunchMapper;
import sm.domain.sys.base.user.quicklaunch.model.entity.UserHomeQuickLaunchEntity;
import sm.domain.sys.base.user.quicklaunch.model.enums.HomeScopeEnum;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class UserHomeQuickLaunchTxService {
    private final UserHomeQuickLaunchMapper mapper;

    void replace(Long userId, HomeScopeEnum scope, Long appId, List<Long> menuIds) {
        // 同一用户的首页偏好串行替换，避免并发保存产生交错排序或部分覆盖。
        mapper.lockUser(userId);
        mapper.deleteScope(userId, scope, appId);
        for (int menuIndex = 0; menuIndex < menuIds.size(); menuIndex++) {
            UserHomeQuickLaunchEntity entity = new UserHomeQuickLaunchEntity();
            entity.setUserId(userId);
            entity.setScopeType(scope);
            entity.setAppId(appId);
            entity.setMenuId(menuIds.get(menuIndex));
            entity.setSeq(menuIndex + 1);
            if (mapper.insert(entity) != 1) {
                throw new BizException(ResultEnum.PERSISTENCE_ERROR, "保存首页快速发起失败");
            }
        }
    }
}
