package sm.domain.sys.base.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.base.app.model.entity.AppEntity;
import sm.domain.sys.base.app.model.form.AppListForm;
import sm.domain.sys.base.app.model.vo.AppDetailVO;
import sm.domain.sys.base.app.model.vo.AppListVO;
import sm.domain.sys.base.app.model.vo.AppVO;
import sm.domain.sys.base.app.model.vo.DomainAppRowVO;

import java.util.List;
import sm.system.query.ListSqlQuery;

@Mapper
public interface AppMapper extends BaseMapper<AppEntity> {
    Page<AppListVO> selectListPage(Page<AppListVO> page, @Param("form") AppListForm form,
                                  @Param("listQuery") ListSqlQuery listQuery);

    AppDetailVO selectDetailById(Long id);

    List<DomainAppRowVO> selectUserDomainApps(@Param("userId") Long userId, @Param("orgId") Long orgId);

    List<DomainAppRowVO> selectAllDomainApps();

    AppVO selectAppByNumber(@Param("appNumber") String appNumber);

    AppVO selectUserAppByNumber(@Param("userId") Long userId, @Param("orgId") Long orgId,
                                @Param("appNumber") String appNumber);
}

