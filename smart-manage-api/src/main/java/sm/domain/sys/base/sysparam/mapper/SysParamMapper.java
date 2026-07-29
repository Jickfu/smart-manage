package sm.domain.sys.base.sysparam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.sysparam.model.entity.SysParamEntity;
import sm.domain.sys.base.sysparam.model.form.SysParamListForm;
import sm.domain.sys.base.sysparam.model.vo.SysParamVO;

/**
 * @author Chekfu
 */
@Mapper
public interface SysParamMapper extends BaseMapper<SysParamEntity> {
    Page<SysParamVO> selectListPage(Page<SysParamVO> page, @Param("form") SysParamListForm form);

    SysParamVO selectDetailById(@Param("id") Long id);
}
