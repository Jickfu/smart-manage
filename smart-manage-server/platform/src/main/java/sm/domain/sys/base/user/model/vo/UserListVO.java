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
	@Schema(description = "ID")
	private Long id;

	@Schema(description = "用户名")
	private String username;

	@Schema(description = "用户姓名")
	private String name;
	@Schema(description = "业务编码")
	private String number;

	@Schema(description = "头像地址")
	private String avatar;
	@Schema(description = "头像附件 ID")
	private Long avatarAttachmentId;

	@Schema(description = "启用状态")
	private Boolean enabled;

	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	@Schema(description = "用户任职明细")
	private List<UserAssignmentVO> assignments = new ArrayList<>();
}
