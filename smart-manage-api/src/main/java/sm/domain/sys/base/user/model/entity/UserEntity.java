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
	private String name;
	private String number;
	private Gender gender;
	private LocalDate birthday;
	/*
	 * 头像地址
	 */
	private Long avatarAttachmentId;
	/*
	 * 邮箱地址
	 */
	private String email;
	private LocalDateTime emailVerifiedAt;
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

	/** 数据库统一维护的凭据代际，不接受普通实体写入或表单赋值。 */
	@TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
	private Long credentialGeneration;

	@Version
	private Integer version;

}
