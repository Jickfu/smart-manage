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

	/** 全局入口与业务应用共用用户锁及排列顺序，键只允许显式白名单。 */
	public void pinInbox(Long userId) {
		mapper.lockUser(userId);
		if (mapper.selectCount(new LambdaQueryWrapper<UserAppPinEntity>()
				.eq(UserAppPinEntity::getUserId, userId)
				.eq(UserAppPinEntity::getBuiltinKey, "builtin:inbox")) > 0) return;
		UserAppPinEntity entity = new UserAppPinEntity();
		entity.setUserId(userId);
		entity.setBuiltinKey("builtin:inbox");
		entity.setSeq(mapper.selectNextSeq(userId));
		if (mapper.insert(entity) != 1) throw new BizException(ResultEnum.PERSISTENCE_ERROR, "固定消息中心失败");
	}

	public void unpinInbox(Long userId) {
		mapper.lockUser(userId);
		mapper.delete(new LambdaQueryWrapper<UserAppPinEntity>()
				.eq(UserAppPinEntity::getUserId, userId)
				.eq(UserAppPinEntity::getBuiltinKey, "builtin:inbox"));
	}
}
