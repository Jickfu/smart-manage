package sm.domain.sys.base.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.system.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import sm.domain.sys.base.user.model.Gender;

/**
 * 用户持久化实体。
 * @author Chekfu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_user")
public class UserEntity extends BaseEntity {
	/**
	 * ID
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * Argon2 密码哈希；不保存明文密码
	 */
	private String password;
	/**
	 * 用户姓名
	 */
	private String name;
	/**
	 * 业务编码
	 */
	private String number;
	/**
	 * 性别
	 */
	private Gender gender;
	/**
	 * 生日
	 */
	private LocalDate birthday;
	/**
	 * 头像附件 ID
	 */
	private Long avatarAttachmentId;
	/**
	 * 邮箱地址
	 */
	private String email;
	/**
	 * 邮箱验证时间
	 */
	private LocalDateTime emailVerifiedAt;
	/**
	 * 手机号码
	 */
	private String phone;
	/**
	 * 主题色
	 */
	private String themeColor;
	/**
	 * 是否必须修改密码
	 */
	private Boolean passwordReset;
	/**
	 * 是否启用
	 */
	private Boolean enabled;

	/**
	 * 数据库统一维护的凭据代际，不接受普通实体写入或表单赋值
	 */
	@TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
	private Long credentialGeneration;

	/**
	 * 乐观锁版本号
	 */
	@Version
	private Integer version;

}
