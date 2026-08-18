package sm.domain.sys.base.user.apppin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import sm.domain.sys.base.user.apppin.mapper.UserAppPinMapper;
import sm.domain.sys.base.user.apppin.model.entity.UserAppPinEntity;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
class UserAppPinTxService {
	private final UserAppPinMapper mapper;

	public void pin(Long userId, Long appId) {
		// 同一用户的固定操作串行计算追加顺序，避免并发固定产生重复排序号。
		mapper.lockUser(userId);
		long existing = mapper.selectCount(new LambdaQueryWrapper<UserAppPinEntity>()
				.eq(UserAppPinEntity::getUserId, userId)
				.eq(UserAppPinEntity::getAppId, appId));
		if (existing > 0) return;
		UserAppPinEntity entity = new UserAppPinEntity();
		entity.setUserId(userId);
		entity.setAppId(appId);
		entity.setSeq(mapper.selectNextSeq(userId));
		if (mapper.insert(entity) != 1) {
			throw new BizException(ResultEnum.PERSISTENCE_ERROR, "固定应用失败");
		}
	}

	public void unpin(Long userId, String appNumber) {
		mapper.deleteByUserAndAppNumber(userId, appNumber);
	}
}
