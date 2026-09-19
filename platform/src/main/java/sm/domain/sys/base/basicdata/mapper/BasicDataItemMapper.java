package sm.domain.sys.base.basicdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.basicdata.model.entity.BasicDataItemEntity;

@Mapper
public interface BasicDataItemMapper extends BaseMapper<BasicDataItemEntity> {
}
