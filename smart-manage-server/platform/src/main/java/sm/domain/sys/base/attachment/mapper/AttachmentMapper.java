package sm.domain.sys.base.attachment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sm.domain.sys.base.attachment.model.entity.AttachmentEntity;

import java.util.List;

/**
 * @author Chekfu
 */
@Mapper
public interface AttachmentMapper extends BaseMapper<AttachmentEntity> {
    @Select("SELECT * FROM t_sys_attachment WHERE id = #{id} FOR UPDATE")
    AttachmentEntity selectForUpdate(@Param("id") Long id);

    List<AttachmentEntity> selectByBiz(@Param("bizType") String bizType, @Param("bizId") String bizId);
}
