package sm.domain.sys.base.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.permission.model.form.PermissionListForm;
import sm.domain.sys.base.permission.model.vo.PermissionListVO;
import sm.system.response.PageData;
import sm.system.query.ListSqlQuery;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionServiceTests {

    @Test
    void listPageReturnsApplicationNameProjectedByMapper() {
        PermissionMapper mapper = mock(PermissionMapper.class);
        PermissionListForm form = new PermissionListForm();
        form.setPageNum(2);
        form.setPageSize(20);
        form.setDomainId(10L);

        PermissionListVO record = new PermissionListVO();
        record.setId(1L);
        record.setAppId(2L);
        record.setAppName("系统管理");
        record.setNumber("sys:base:permission:listPage");
        record.setName("权限列表");
        Page<PermissionListVO> mapperPage = new Page<>(2, 20, 21);
        mapperPage.setRecords(List.of(record));
        when(mapper.selectListPage(any(Page.class), same(form), any(ListSqlQuery.class))).thenReturn(mapperPage);

        PermissionService service = new PermissionService(mapper, mock(PermissionTxService.class),
                mock(sm.domain.sys.base.common.helper.AuthorizationStateHelper.class));

        PageData<PermissionListVO> result = service.listPage(form);

        assertEquals(21, result.getTotal());
        assertEquals(2, result.getPageNum());
        assertEquals(20, result.getPageSize());
        assertEquals("系统管理", result.getRecords().getFirst().getAppName());
        verify(mapper).selectListPage(any(Page.class), same(form), any(ListSqlQuery.class));
    }
}
