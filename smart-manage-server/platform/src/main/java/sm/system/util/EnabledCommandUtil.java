package sm.system.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

/**
 * 启停命令工具，只更新 enabled 字段，避免用完整表单覆盖其他业务数据。
 */
public final class EnabledCommandUtil {
	private EnabledCommandUtil() {
	}

	public static <T> void update(BaseMapper<T> mapper, SFunction<T, ?> idColumn,
			SFunction<T, ?> enabledColumn, List<Long> ids, boolean enabled, String resourceName) {
		List<Long> distinctIds = ids.stream().distinct().toList();
		long existingCount = mapper.selectCount(new LambdaQueryWrapper<T>().in(idColumn, distinctIds));
		if (existingCount != distinctIds.size()) {
			throw new BizException(ResultEnum.NOT_FOUND, resourceName + "不存在");
		}
		int affectedRows = mapper.update(new LambdaUpdateWrapper<T>()
				.in(idColumn, distinctIds)
				.set(enabledColumn, enabled));
		if (affectedRows != distinctIds.size()) {
			throw new BizException(ResultEnum.DATA_CONFLICT, resourceName + "状态已发生变化，请刷新后重试");
		}
	}
}
