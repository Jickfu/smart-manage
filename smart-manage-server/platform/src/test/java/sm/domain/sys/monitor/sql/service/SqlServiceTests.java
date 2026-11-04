package sm.domain.sys.monitor.sql.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.monitor.sql.converter.SqlLogConverter;
import sm.domain.sys.monitor.sql.mapper.SqlCatalogMapper;
import sm.domain.sys.monitor.sql.mapper.SqlLogMapper;
import sm.domain.sys.monitor.sql.model.form.SqlCatalogListForm;
import sm.domain.sys.monitor.sql.model.form.SqlCatalogObjectForm;
import sm.domain.sys.monitor.sql.model.vo.SqlCatalogObjectVO;
import sm.system.exception.BizException;
import sm.system.security.context.CurrentUserContext;
import sm.system.web.ClientIpResolver;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SqlServiceTests {

    @Test
    void usesDefaultMaxRowsWhenParameterIsMissing() {
        SysParamService sysParamService = mock(SysParamService.class);
        when(sysParamService.getInt(SqlService.MAX_ROWS_PARAMETER)).thenReturn(null);
        SqlService service = createService(sysParamService);
        assertEquals(1000, service.resolveMaxRows());
    }

    @Test
    void rejectsMaxRowsAboveHardLimit() {
        SysParamService sysParamService = mock(SysParamService.class);
        when(sysParamService.getInt(SqlService.MAX_ROWS_PARAMETER)).thenReturn(5001);
        SqlService service = createService(sysParamService);
        assertThrows(BizException.class, service::resolveMaxRows);
    }

    @Test
    void normalizesCatalogKeywordAndReportsTruncation() {
        SqlCatalogMapper catalogMapper = mock(SqlCatalogMapper.class);
        List<SqlCatalogObjectVO> objects = new ArrayList<>();
        for (int index = 0; index <= SqlService.CATALOG_MAX_OBJECTS; index++) {
            objects.add(new SqlCatalogObjectVO());
        }
        when(catalogMapper.selectObjects("用户", SqlService.CATALOG_MAX_OBJECTS + 1)).thenReturn(objects);
        SqlService service = createService(mock(SysParamService.class), catalogMapper);
        SqlCatalogListForm form = new SqlCatalogListForm();
        form.setKeyword("  用户  ");

        var result = service.catalog(form);

        assertEquals(SqlService.CATALOG_MAX_OBJECTS, result.getObjects().size());
        assertTrue(result.isTruncated());
    }

    @Test
    void rejectsMissingCatalogObjectWithoutReadingColumns() {
        SqlCatalogMapper catalogMapper = mock(SqlCatalogMapper.class);
        SqlService service = createService(mock(SysParamService.class), catalogMapper);
        SqlCatalogObjectForm form = new SqlCatalogObjectForm();
        form.setSchemaName("public");
        form.setObjectName("missing_table");

        assertThrows(BizException.class, () -> service.catalogDetail(form));
        verify(catalogMapper).selectObject("public", "missing_table");
        verify(catalogMapper, never()).selectColumns("public", "missing_table");
    }

    private SqlService createService(SysParamService sysParamService) {
        return createService(sysParamService, mock(SqlCatalogMapper.class));
    }

    private SqlService createService(SysParamService sysParamService, SqlCatalogMapper catalogMapper) {
        return new SqlService(
                mock(SqlLogMapper.class),
                catalogMapper,
                mock(SqlExecutionTxService.class),
                mock(SqlLogConverter.class),
                mock(CurrentUserContext.class),
                sysParamService,
                mock(ClientIpResolver.class)
        );
    }
}
