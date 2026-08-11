package sm.domain.sys.base.org.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.org.mapper.OrgMapper;
import sm.domain.sys.base.org.model.OrgType;
import sm.domain.sys.base.org.model.entity.OrgEntity;
import sm.domain.sys.base.org.model.form.OrgSaveForm;
import sm.system.exception.BizException;
import sm.system.response.ResultEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrgTxServiceTests {

    @Test
    void archiveDisablesOrganizationAndRecordsArchiveTime() {
        OrgMapper mapper = mock(OrgMapper.class);
        OrgEntity organization = organization(1L, null, false);
        organization.setEnabled(true);
        when(mapper.selectByIds(List.of(1L))).thenReturn(List.of(organization));
        when(mapper.selectList(null)).thenReturn(List.of(organization));
        when(mapper.updateById(organization)).thenReturn(1);

        new OrgTxService(mapper).archive(List.of(1L));

        assertFalse(organization.getEnabled());
        assertTrue(organization.getArchived());
        assertNotNull(organization.getArchivedAt());
        verify(mapper).updateById(organization);
    }

    @Test
    void archiveRejectsOrganizationWithUnarchivedDescendant() {
        OrgMapper mapper = mock(OrgMapper.class);
        OrgEntity parent = organization(1L, null, false);
        OrgEntity child = organization(2L, 1L, false);
        when(mapper.selectByIds(List.of(1L))).thenReturn(List.of(parent));
        when(mapper.selectList(null)).thenReturn(List.of(parent, child));

        BizException exception = assertThrows(BizException.class,
                () -> new OrgTxService(mapper).archive(List.of(1L)));

        assertEquals(ResultEnum.PARAM_ERROR.getCode(), exception.getCode());
        verify(mapper, never()).updateById(any(OrgEntity.class));
    }

    @Test
    void unarchiveKeepsOrganizationDisabledAndClearsArchiveTime() {
        OrgMapper mapper = mock(OrgMapper.class);
        OrgEntity organization = organization(1L, null, true);
        organization.setEnabled(false);
        organization.setArchivedAt(java.time.LocalDateTime.now());
        when(mapper.selectByIds(List.of(1L))).thenReturn(List.of(organization));
        when(mapper.updateById(organization)).thenReturn(1);

        new OrgTxService(mapper).unarchive(List.of(1L));

        assertFalse(organization.getEnabled());
        assertFalse(organization.getArchived());
        assertNull(organization.getArchivedAt());
    }

    @Test
    void saveRejectsStaleVersionBeforeWriting() {
        OrgMapper mapper = mock(OrgMapper.class);
        OrgEntity organization = organization(1L, null, false);
        organization.setVersion(2);
        when(mapper.selectById(1L)).thenReturn(organization);
        OrgSaveForm form = new OrgSaveForm();
        form.setId(1L);
        form.setVersion(1);
        form.setNumber("ORG");
        form.setName("组织");
        form.setOrgType(OrgType.COMPANY);

        BizException exception = assertThrows(BizException.class,
                () -> new OrgTxService(mapper).save(form));

        assertEquals(ResultEnum.DATA_CONFLICT.getCode(), exception.getCode());
        verify(mapper, never()).updateById(any(OrgEntity.class));
    }

    private static OrgEntity organization(Long id, Long parentId, boolean archived) {
        OrgEntity organization = new OrgEntity();
        organization.setId(id);
        organization.setParentId(parentId);
        organization.setNumber("ORG-" + id);
        organization.setName("组织" + id);
        organization.setNumberPath("ORG-" + id);
        organization.setNamePath("组织" + id);
        organization.setOrgType(OrgType.COMPANY);
        organization.setArchived(archived);
        organization.setVersion(0);
        return organization;
    }
}
