package sm.domain.sys.base.attachment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;

import java.util.List;

/**
 * @author Chekfu
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<AttachmentEntity> {
    List<AttachmentEntity> selectByBiz(@Param("bizType") String bizType, @Param("bizId") String bizId);
}
