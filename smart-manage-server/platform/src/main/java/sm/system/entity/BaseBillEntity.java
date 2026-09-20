package sm.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 业务单据实体基类。
 *
 * <p>只有具备保存、提交、审核等生命周期的业务单据才继承该类；日志、配置、关系表等非业务单据继续继承 {@link BaseEntity}。
 *
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseBillEntity extends BaseEntity {
    /**
     * 单据所属组织。该字段表达业务数据归属，不等同于创建人的当前登录组织。
     */
    private Long orgId;

    /**
	 * 业务日期：单据最核心的业务发生或归属日期。
	 */
	private LocalDate bizDate;

	/**
	 * 单据状态：A 暂存，B 已提交，C 审核通过，D 已关闭
	 */
	private String billStatus;
}
