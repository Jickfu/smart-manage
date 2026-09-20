package sm.system.enums;

import lombok.Getter;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.Arrays;

/**
 * 业务单据状态。
 *
 * @author Chekfu
 */
@Getter
public enum BillStatusEnum {
	SAVED("A", "暂存"),
	SUBMITTED("B", "已提交"),
	AUDITED("C", "审核通过"),
	CLOSED("D", "已关闭");

	private final String value;
	private final String name;

	BillStatusEnum(String value, String name) {
		this.value = value;
		this.name = name;
	}

	public static BillStatusEnum fromValue(String value) {
		return Arrays.stream(values())
				.filter(status -> status.value.equals(value))
				.findFirst()
				.orElseThrow(() -> new BizException(ResultEnum.BILL_STATUS_ERROR, "未知单据状态：" + value));
	}

	public boolean isSaved() {
		return this == SAVED;
	}
}
