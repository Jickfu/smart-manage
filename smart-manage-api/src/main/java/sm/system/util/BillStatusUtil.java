package sm.system.util;

import cn.hutool.core.util.StrUtil;
import sm.system.enums.BillStatusEnum;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

/**
 * 单据状态工具类。
 *
 * <p>这里只做纯状态判断，不访问数据库。具体单据的 Service 必须先读取数据库当前状态，再调用这些方法校验，
 * 不能直接相信前端传入的单据状态。
 *
 * @author Chekfu
 */
public class BillStatusUtil {

	private BillStatusUtil() {
	}

	public static String defaultSaved(String billStatus) {
		if (StrUtil.isBlank(billStatus)) {
			return BillStatusEnum.SAVED.getValue();
		}
		return billStatus;
	}

	public static void requireCanSave(String currentBillStatus) {
		if (StrUtil.isBlank(currentBillStatus)) {
			return;
		}
		BillStatusEnum currentStatus = BillStatusEnum.fromValue(currentBillStatus);
		if (!currentStatus.isSaved()) {
			throw new BizException(ResultEnum.BILL_STATUS_ERROR, "只有暂存状态的单据允许保存");
		}
	}

	public static void requireCanSubmit(String currentBillStatus) {
		if (StrUtil.isBlank(currentBillStatus)) {
			throw new BizException(ResultEnum.BILL_STATUS_ERROR, "单据状态为空，不能提交");
		}
		BillStatusEnum currentStatus = BillStatusEnum.fromValue(currentBillStatus);
		if (!currentStatus.isSaved()) {
			throw new BizException(ResultEnum.BILL_STATUS_ERROR, "只有暂存状态的单据允许提交");
		}
	}

	public static String submit(String currentBillStatus) {
		requireCanSubmit(currentBillStatus);
		return BillStatusEnum.SUBMITTED.getValue();
	}
}
