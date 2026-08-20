package sm.domain.sys.base.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.domain.model.entity.DomainEntity;

@Mapper
public interface DomainMapper extends BaseMapper<DomainEntity> {
}

