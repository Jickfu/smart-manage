package sm.domain.sys.base.user.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


/**
 * @author Chekfu
 */
@Data
@Schema(description = "用户保存表单")
public class UserSaveForm {

	@Schema(description = "id")
	private Long id;

	@Schema(description = "乐观锁版本号，修改时必传")
	private Integer version;

	@Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "用户名不能为空")
	private String username;

	@Schema(description = "新增用户的初始密码，编辑时禁止传入")
	private String password;

	@Schema(description = "昵称")
	private String nickname;

	@Schema(description = "邮箱")
	@Email(message = "邮箱格式不正确")
	private String email;

	@Schema(description = "手机号")
	@Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
	private String phone;

	@Schema(description = "头像地址")
	private String avatar;

}
