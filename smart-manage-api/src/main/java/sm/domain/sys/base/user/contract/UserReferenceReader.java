package sm.domain.sys.base.user.contract;

import java.util.Collection;
import java.util.Map;

/** 供其他业务领域读取和校验系统用户引用的稳定契约。 */
public interface UserReferenceReader {

    /** 返回仍然存在的用户；用户已禁用时仍可用于历史引用读取。 */
    UserReference require(Long userId);

    /** 返回可用于新增或修改业务引用的启用用户。 */
    UserReference requireEnabled(Long userId);

    /** 批量返回仍然存在的用户，允许缺失，结果按去重后的输入顺序排列。 */
    Map<Long, UserReference> findByIds(Collection<Long> userIds);

    /** 一次批量查询并要求全部用户存在且启用，结果按去重后的输入顺序排列。 */
    Map<Long, UserReference> requireEnabledByIds(Collection<Long> userIds);
}
