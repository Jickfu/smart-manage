package sm.domain.sys.message.inbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.message.inbox.model.entity.InboxMessageEntity;
import sm.domain.sys.message.inbox.model.form.InboxMessageListForm;
import sm.domain.sys.message.inbox.model.vo.InboxMessageListVO;
import sm.system.query.ListSqlQuery;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InboxMessageMapper extends BaseMapper<InboxMessageEntity> {
    Page<InboxMessageListVO> selectAdminListPage(Page<InboxMessageListVO> page,
            @Param("form") InboxMessageListForm form, @Param("listQuery") ListSqlQuery listQuery);

    List<InboxMessageEntity> claimPending(@Param("staleBefore") LocalDateTime staleBefore,
            @Param("batchSize") int batchSize);

    int completePublish(@Param("id") Long id, @Param("publishTime") LocalDateTime publishTime,
            @Param("recipientCount") long recipientCount);

    int markFailed(@Param("id") Long id, @Param("errorMessage") String errorMessage);
}
