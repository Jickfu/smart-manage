package sm.domain.sys.base.passwordconfig.converter;

import org.mapstruct.Mapper;
import sm.infrastructure.mapping.SmMapperConfig;
import sm.domain.sys.base.passwordconfig.model.entity.PasswordConfigEntity;
import sm.domain.sys.base.passwordconfig.model.vo.PasswordConfigDetailVO;

/** 密码策略管理详情纯字段转换。 */
@Mapper(config = SmMapperConfig.class)
public interface PasswordConfigConverter {
    PasswordConfigDetailVO toDetailVO(PasswordConfigEntity entity);
}
