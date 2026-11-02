package sm.domain.sys.base.passwordconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import sm.domain.sys.base.passwordconfig.model.entity.PasswordConfigEntity;

@Mapper
public interface PasswordConfigMapper extends BaseMapper<PasswordConfigEntity> {
    // 绕过 MyBatis 会话缓存，长事务内也不能复用已经变更的密码策略。
    @Select("SELECT * FROM t_sys_password_config WHERE id = 1")
    @Options(useCache = false, flushCache = Options.FlushCachePolicy.TRUE)
    PasswordConfigEntity current();
}
