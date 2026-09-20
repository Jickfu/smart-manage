package sm.domain.sys.base.basicdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sm.domain.sys.base.basicdata.model.entity.BasicDataCategoryEntity;

@Mapper
public interface BasicDataCategoryMapper extends BaseMapper<BasicDataCategoryEntity> {
    /** 将分类行作为整棵基础数据树的事务互斥点。 */
    @Select("SELECT * FROM t_sys_basic_data_category WHERE id = #{id} FOR UPDATE")
    BasicDataCategoryEntity selectForUpdate(@Param("id") Long id);
}
