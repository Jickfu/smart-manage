package sm.domain.sys.base.weakpassword.converter;

import org.mapstruct.Mapper;
import sm.domain.sys.base.weakpassword.model.entity.WeakPasswordEntity;
import sm.domain.sys.base.weakpassword.model.vo.WeakPasswordVO;

@Mapper(componentModel = "spring")
public interface WeakPasswordConverter {
    WeakPasswordVO toVO(WeakPasswordEntity entity);
}
