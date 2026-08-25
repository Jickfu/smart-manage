package sm.domain.sys.base.role.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeMapper;
import sm.domain.sys.base.datascope.mapper.RoleDataScopeOrgMapper;
import sm.domain.sys.base.role.mapper.RoleMapper;
import sm.domain.sys.base.role.mapper.RolePermissionMapper;
import sm.domain.sys.base.role.model.entity.RoleEntity;
import sm.domain.sys.base.role.model.form.RoleSaveForm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleTxServiceTests {

    @Test
    void createUsesSafeSelfScopeWithoutAcceptingItFromRoleSave() {
        RoleMapper mapper = mock(RoleMapper.class);
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.insert(any(RoleEntity.class))).thenAnswer(invocation -> {
            RoleEntity entity = invocation.getArgument(0);
            assertEquals("SELF", entity.getDefaultDataScope());
            entity.setId(10L);
            return 1;
        });
        RoleTxService service = service(mapper);
        RoleSaveForm form = form(null, null);

        assertEquals(10L, service.save(form));
    }

    @Test
    void updatePreservesExistingDataScope() {
        RoleMapper mapper = mock(RoleMapper.class);
        RoleEntity entity = new RoleEntity();
        entity.setId(10L);
        entity.setVersion(2);
        entity.setNumber("buyer");
        entity.setDefaultDataScope("ORG");
        when(mapper.selectCount(any())).thenReturn(0L);
        when(mapper.selectById(10L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(1);

        service(mapper).save(form(10L, 2));

        assertEquals("ORG", entity.getDefaultDataScope());
        verify(mapper).updateById(entity);
    }

    private RoleTxService service(RoleMapper mapper) {
        return new RoleTxService(mapper, mock(RolePermissionMapper.class), mock(RoleDataScopeMapper.class),
                mock(RoleDataScopeOrgMapper.class));
    }

    private RoleSaveForm form(Long id, Integer version) {
        RoleSaveForm form = new RoleSaveForm();
        form.setId(id);
        form.setVersion(version);
        form.setNumber("buyer");
        form.setName("采购员");
        form.setDescription("采购角色");
        return form;
    }
}
