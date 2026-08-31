package sm.domain.sys.base.openapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiGrantEntity;

@Mapper
public interface OpenApiGrantMapper extends BaseMapper<OpenApiGrantEntity> {
}
