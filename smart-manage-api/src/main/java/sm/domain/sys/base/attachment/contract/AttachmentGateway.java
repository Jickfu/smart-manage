package sm.domain.sys.base.attachment.contract;

import sm.domain.sys.base.attachment.contract.model.form.AttachmentPromoteForm;
import sm.domain.sys.base.attachment.contract.model.vo.AttachmentVO;

import java.io.IOException;
import java.util.List;

/** 供其他业务领域绑定、读取和清理聚合附件的稳定契约。 */
public interface AttachmentGateway {

    void promoteForAggregate(AttachmentPromoteForm form) throws IOException;

    void deleteForAggregate(String bizType, String bizId) throws IOException;

    List<AttachmentVO> listByBiz(String bizType, String bizId);
}
