package sm.domain.sys.base.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Redis 用户查询快照，仅包含当前缓存消费者实际需要的非认证字段。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCacheSnapshot implements Serializable {
    private Long id;
    private String username;
    private String name;
    private Long avatarAttachmentId;
}
