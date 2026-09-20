package sm.domain.sys.monitor.loginlog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.monitor.loginlog.model.entity.LoginLogEntity;

@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLogEntity> {
}

