package sm.domain.sys.base.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.domain.sys.base.permission.model.form.PermissionListForm;
import sm.domain.sys.base.permission.model.vo.PermissionListVO;
import sm.domain.sys.base.permission.model.vo.PermissionDetailVO;
import sm.domain.sys.base.permission.model.vo.PermissionSelectVO;
import sm.domain.sys.base.permission.model.form.PermissionSelectForm;

import java.util.List;
import sm.system.query.ListSqlQuery;

/**
 * @author Chekfu
 */
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionEntity> {
    Page<PermissionListVO> selectListPage(Page<PermissionListVO> page,
                                          @Param("form") PermissionListForm form,
                                          @Param("listQuery") ListSqlQuery listQuery);
    List<PermissionSelectVO> selectAll();
    Page<PermissionSelectVO> selectPage(Page<PermissionSelectVO> page,
                                        @Param("form") PermissionSelectForm form);
    PermissionDetailVO selectDetailById(@Param("id") Long id);

    List<String> selectUserPermissionNumbers(@Param("userId") Long userId,
                                             @Param("orgId") Long orgId,
                                             @Param("prefix") String prefix);
}
