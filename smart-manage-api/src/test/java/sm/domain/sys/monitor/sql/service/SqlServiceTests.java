package sm.domain.sys.monitor.sql.service;

import org.junit.jupiter.api.Test;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.sysparam.service.SysParamService;
import sm.domain.sys.monitor.sql.mapper.SqlLogMapper;
import sm.system.exception.BizException;
import sm.system.web.ClientIpResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
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

    private SqlService createService(SysParamService sysParamService) {
        return new SqlService(
                mock(SqlLogMapper.class),
                mock(SqlExecutionTxService.class),
                mock(SqlLogConverter.class),
                mock(CurrentUserContext.class),
                sysParamService,
                mock(ClientIpResolver.class)
        );
    }
}
