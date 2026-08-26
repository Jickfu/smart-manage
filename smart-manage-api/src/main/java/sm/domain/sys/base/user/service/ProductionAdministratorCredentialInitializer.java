package sm.domain.sys.base.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sm.domain.sys.base.user.mapper.UserMapper;
import sm.domain.sys.base.user.model.entity.UserEntity;
import sm.system.helper.Argon2Helper;

/**
 * 生产环境管理员初始凭据收口。
 *
 * <p>Flyway 中的固定凭据只服务于演示环境。生产启动时必须显式提供独立密码；
 * 只有数据库仍使用演示密码时才执行替换，避免重启覆盖管理员后续主动修改的密码。
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
class ProductionAdministratorCredentialInitializer implements ApplicationRunner {

    private static final String ADMINISTRATOR = "administrator";
    private static final String DEMO_PASSWORD = "admin";

    private final UserMapper userMapper;

    @Value("${smart-manage.domain.sys.base.user.initial-administrator-password}")
    private String initialPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (initialPassword == null || initialPassword.isBlank() || DEMO_PASSWORD.equals(initialPassword)) {
            throw new IllegalStateException("生产环境必须配置非演示值的初始管理员密码");
        }

        UserEntity administrator = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, ADMINISTRATOR));
        if (administrator == null) {
            throw new IllegalStateException("生产环境缺少 administrator 管理员账号");
        }
        if (!Argon2Helper.verify(administrator.getPassword(), DEMO_PASSWORD)) {
            log.info("生产管理员已使用非演示密码，跳过初始凭据替换");
            return;
        }

        String encodedPassword = Argon2Helper.encode(initialPassword);
        int updated = userMapper.update(new LambdaUpdateWrapper<UserEntity>()
                .set(UserEntity::getPassword, encodedPassword)
                .eq(UserEntity::getId, administrator.getId())
                .eq(UserEntity::getPassword, administrator.getPassword()));
        if (updated != 1) {
            throw new IllegalStateException("生产管理员初始密码替换失败");
        }
        log.info("生产管理员演示密码已替换");
    }
}
