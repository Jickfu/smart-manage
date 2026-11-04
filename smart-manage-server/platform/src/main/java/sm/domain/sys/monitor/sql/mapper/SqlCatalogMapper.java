package sm.domain.sys.monitor.sql.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sm.domain.sys.monitor.sql.model.vo.SqlCatalogColumnVO;
import sm.domain.sys.monitor.sql.model.vo.SqlCatalogObjectVO;

import java.util.List;

@Mapper
public interface SqlCatalogMapper {
    List<SqlCatalogObjectVO> selectObjects(@Param("keyword") String keyword, @Param("limit") int limit);

    SqlCatalogObjectVO selectObject(@Param("schemaName") String schemaName,
                                    @Param("objectName") String objectName);

    List<SqlCatalogColumnVO> selectColumns(@Param("schemaName") String schemaName,
                                           @Param("objectName") String objectName);
}
