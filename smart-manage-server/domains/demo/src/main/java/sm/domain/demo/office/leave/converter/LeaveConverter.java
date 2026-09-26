package sm.domain.demo.office.leave.converter;
import org.mapstruct.*;
import sm.infrastructure.mapping.SmMapperConfig;
import sm.domain.demo.office.leave.model.entity.LeaveEntity;
import sm.domain.demo.office.leave.model.vo.LeaveDetailVO;
@Mapper(config = SmMapperConfig.class)
public interface LeaveConverter {
    @Mapping(target = "entries", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "retainedAttachmentIds", ignore = true)
    LeaveDetailVO toDetailVO(LeaveEntity entity);
}
