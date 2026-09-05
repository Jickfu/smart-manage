package sm.domain.sys.message.inbox.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.message.inbox.model.form.InboxCursorListForm;
import sm.domain.sys.message.inbox.model.form.InboxReceiptKeyForm;
import sm.domain.sys.message.inbox.model.vo.InboxDetailVO;
import sm.domain.sys.message.inbox.model.vo.InboxItemVO;

import java.time.LocalDateTime;
import sm.system.query.ListSqlQuery;
import java.util.List;

@Mapper
public interface InboxRecipientMapper {
    int insertEnabledRecipients(@Param("messageId") Long messageId,
            @Param("receivedTime") LocalDateTime receivedTime);

    int insertSelectedRecipients(@Param("messageId") Long messageId,
            @Param("receivedTime") LocalDateTime receivedTime, @Param("userIds") List<Long> userIds);

    List<InboxItemVO> selectCursorPage(@Param("userId") Long userId,
            @Param("beginTime") LocalDateTime beginTime, @Param("form") InboxCursorListForm form,
            @Param("limit") int limit, @Param("listQuery") ListSqlQuery listQuery);

    InboxDetailVO selectDetail(@Param("userId") Long userId, @Param("messageId") Long messageId,
            @Param("receivedTime") String receivedTime);

    int updateReadStatus(@Param("userId") Long userId,
            @Param("receipts") List<InboxReceiptKeyForm> receipts,
            @Param("readStatus") boolean readStatus, @Param("readTime") LocalDateTime readTime);

    int markAllRead(@Param("userId") Long userId, @Param("beginTime") LocalDateTime beginTime,
            @Param("readTime") LocalDateTime readTime);

    int countUnreadCapped(@Param("userId") Long userId, @Param("beginTime") LocalDateTime beginTime,
            @Param("limit") int limit);
    int countUnreadByAudienceCapped(@Param("userId") Long userId, @Param("beginTime") LocalDateTime beginTime,
            @Param("limit") int limit, @Param("audienceType") String audienceType);
}
