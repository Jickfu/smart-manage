package sm.domain.sys.base.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chekfu
 */
@Data
@Schema(description = "用户列表视图")
public class UserListVO {
	@Schema(description = "id")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "昵称")
	private String name;
	private String number;

	@Schema(description = "头像地址")
	private String avatar;
	private Long avatarAttachmentId;

	@Schema(description = "启用状态")
	private Boolean enabled;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	private List<UserAssignmentVO> assignments = new ArrayList<>();
}
