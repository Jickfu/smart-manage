package sm.domain.sys.base.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

/**
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user")
public class UserEntity extends BaseEntity {
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	/*
	 * 用户名
	 */
	private String username;
	/*
	 * 密码
	 */
	private String password;
	/*
	 * 昵称
	 */
	private String nickname;
	/*
	 * 头像地址
	 */
	private String avatar;
	/*
	 * 邮箱地址
	 */
	private String email;
	/*
	 * 手机号
	 */
	private String phone;
	/*
	 * 主题颜色
	 */
	private String themeColor;
	/*
	 * 是否必须修改密码
	 */
	private Boolean passwordReset;
	/*
	 * 是否可用
	 */
	private Boolean enabled;

	@Version
	private Integer version;

}
