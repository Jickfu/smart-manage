package sm.domain.sys.base.attachment.contract;

import java.io.IOException;
import java.util.List;

/** 供其他业务领域绑定、读取和清理聚合附件的稳定契约。 */
public interface AttachmentGateway {

    void promoteForAggregate(AttachmentPromoteCommand command) throws IOException;

    void deleteForAggregate(String bizType, String bizId) throws IOException;

    List<AttachmentReference> listByBiz(String bizType, String bizId);

    /** 已校验业务写权限的聚合在同一事务冻结自身附件快照，不额外要求页面详情权限。 */
    List<AttachmentReference> listForAggregate(String bizType, String bizId);
}
