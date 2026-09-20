package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.domain.sys.base.weakpassword.service.PasswordPolicyService;
import sm.system.helper.Argon2Helper;

/** 所有环境统一的一次性管理员初始化；空密码仅是不可登录的未初始化标记。 */
@Component
@RequiredArgsConstructor
@Slf4j
class AdministratorCredentialInitializer implements ApplicationRunner {
    private final UserMapper userMapper;
    private final PasswordPolicyService passwordPolicyService;
    private final AdministratorInitialPasswordFile initialPasswordFile;

    @Override
    public void run(ApplicationArguments arguments) {
        UserEntity administrator = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, "administrator"));
        if (administrator == null) { throw new IllegalStateException("缺少 administrator 管理员账号"); }
        // 已初始化时不再生成或读取临时密码文件，重启不能修改既有凭据。
        if (!"".equals(administrator.getPassword())) { return; }
        AdministratorInitialPasswordFile.PreparedPassword preparedPassword = initialPasswordFile.prepare();
        String initialPassword = preparedPassword.password();
        passwordPolicyService.validate(initialPassword, "administrator");
        String encoded = Argon2Helper.encode(initialPassword);
        // 数据库条件更新保证多实例竞争只有一次生效，不覆盖并发初始化或主动改密。
        int changed = userMapper.update(new LambdaUpdateWrapper<UserEntity>()
                .set(UserEntity::getPassword, encoded).set(UserEntity::getPasswordReset, true)
                .eq(UserEntity::getId, administrator.getId()).eq(UserEntity::getPassword, ""));
        if (changed != 1) {
            UserEntity current = userMapper.selectById(administrator.getId());
            if (current == null || current.getPassword() == null || current.getPassword().isBlank()) {
                throw new IllegalStateException("管理员初始化未完成");
            }
            // 不同工作目录下的并发实例可能各自生成文件，失败实例不能留下无效凭据。
            if (!Argon2Helper.verify(current.getPassword(), initialPassword)) {
                initialPasswordFile.deleteIfMatches(preparedPassword);
                return;
            }
        }
        log.warn("首次安装已生成 administrator 临时密码文件，请读取后妥善删除: {}",
                preparedPassword.path());
    }
}
