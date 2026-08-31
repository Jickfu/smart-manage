package sm.domain.sys.base.openapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sm.domain.sys.base.openapi.model.entity.OpenApiApplicationEntity;

@Mapper
public interface OpenApiApplicationMapper extends BaseMapper<OpenApiApplicationEntity> {
}
