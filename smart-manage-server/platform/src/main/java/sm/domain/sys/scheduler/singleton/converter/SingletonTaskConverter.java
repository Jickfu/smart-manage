package sm.domain.sys.scheduler.singleton.converter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sm.infrastructure.mapping.SmMapperConfig;
import sm.domain.sys.scheduler.singleton.model.entity.SingletonTaskEntity;
import sm.domain.sys.scheduler.singleton.model.vo.SingletonTaskVO;
/** 单例任务字段映射；运行新鲜度和代码可用性由业务入口组装。 */
@Mapper(config = SmMapperConfig.class)
public interface SingletonTaskConverter {
    @Mapping(target = "available", ignore = true)
    SingletonTaskVO toVO(SingletonTaskEntity entity);
}
