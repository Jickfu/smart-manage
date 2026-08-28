package sm.domain.sys.base.menu.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.system.security.context.CurrentUserContext;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.permission.mapper.PermissionMapper;
import sm.domain.sys.base.feature.mapper.FeatureMapper;
import sm.domain.sys.base.feature.model.entity.FeatureEntity;
import sm.domain.sys.base.permission.model.entity.PermissionEntity;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuSaveForm;
import sm.domain.sys.base.menu.model.enums.ExternalOpenModeEnum;
import sm.domain.sys.base.menu.model.enums.MenuTargetTypeEnum;
import sm.system.exception.BizException;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuTxServiceTests {
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final MenuMapper mapper = mock(MenuMapper.class);
    private final PermissionMapper permissionMapper = mock(PermissionMapper.class);
    private final FeatureMapper featureMapper = mock(FeatureMapper.class);
    private final MenuTxService txService = new MenuTxService(currentUserContext, mapper, permissionMapper, featureMapper);

    @Test
    void staleVersionCannotOverwriteMenu() {
        MenuEntity entity = new MenuEntity();
        entity.setId(1L);
        entity.setVersion(2);
        when(mapper.selectById(1L)).thenReturn(entity);
        MenuSaveForm form = validEditForm();
        form.setVersion(1);

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).updateAllColumns(any());
    }

    @Test
    void concurrentUpdateReturningZeroIsConflict() {
        MenuEntity entity = new MenuEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setNumber("menu_management");
        entity.setEnabled(true);
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateAllColumns(any())).thenReturn(0);
        MenuSaveForm form = validEditForm();
        form.setVersion(2);

        assertThrows(BizException.class, () -> txService.save(form));
    }

    @Test
    void menuWithChildrenCannotBeDeleted() {
        MenuEntity entity = new MenuEntity();
        entity.setId(1L);
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.selectCount(any())).thenReturn(1L);

        assertThrows(BizException.class, () -> txService.deleteById(1L));
        verify(mapper, never()).deleteById(1L);
    }

    @Test
    void menuNumberMustUseLowerSnakeCase() {
        MenuSaveForm form = validEditForm();
        form.setId(null);
        form.setNumber("User-Management");

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).updateAllColumns(any());
    }

    @Test
    void existingMenuNumberCannotBeChanged() {
        MenuEntity entity = new MenuEntity();
        entity.setId(1L);
        entity.setVersion(2);
        entity.setNumber("original_menu");
        when(mapper.selectById(1L)).thenReturn(entity);
        MenuSaveForm form = validEditForm();
        form.setVersion(2);

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).updateAllColumns(any());
    }

    @Test
    void menuNumberMustBeUniqueWithinApplication() {
        MenuSaveForm form = validEditForm();
        form.setId(null);
        when(mapper.selectCount(any())).thenReturn(1L);

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).insert(any(MenuEntity.class));
    }

    @Test
    void menuPermissionMustBelongToSelectedFeature() {
        MenuSaveForm form = validEditForm();
        form.setId(null);
        form.setPermissionId(200L);
        PermissionEntity permission = new PermissionEntity();
        permission.setId(200L);
        permission.setFeatureId(101L);
        when(permissionMapper.selectById(200L)).thenReturn(permission);

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).insert(any(MenuEntity.class));
    }

    @Test
    void menuLevelsUseZeroAndOne() {
        assertEquals(0, MenuLevelEnum.CATEGORY.getCode());
        assertEquals(1, MenuLevelEnum.PAGE.getCode());
    }

    @Test
    void rootPageMenuCanBeSavedWithoutAGroup() {
        MenuSaveForm form = new MenuSaveForm();
        form.setNumber("user_management");
        form.setName("用户管理");
        form.setLevel(MenuLevelEnum.PAGE);
        form.setParentId(0L);
        form.setAppId(31L);
        form.setFeatureId(100L);
        form.setPermissionId(200L);
        form.setPath("/sys/base/user");
        form.setComponent("sys/base/user");
        form.setTargetType(MenuTargetTypeEnum.INTERNAL_PAGE);

        FeatureEntity feature = new FeatureEntity();
        feature.setId(100L);
        feature.setAppId(31L);
        PermissionEntity permission = new PermissionEntity();
        permission.setId(200L);
        permission.setFeatureId(100L);
        when(featureMapper.selectById(100L)).thenReturn(feature);
        when(permissionMapper.selectById(200L)).thenReturn(permission);
        when(mapper.insert(any(MenuEntity.class))).thenReturn(1);

        txService.save(form);

        verify(mapper).insert(any(MenuEntity.class));
    }

    @Test
    void externalLinkMenuSupportsHttpAndIframe() {
        MenuSaveForm form = validExternalLinkForm("http://internal.example.test/home");
        when(mapper.insert(any(MenuEntity.class))).thenReturn(1);

        txService.save(form);

        ArgumentCaptor<MenuEntity> entityCaptor = ArgumentCaptor.forClass(MenuEntity.class);
        verify(mapper).insert(entityCaptor.capture());
        MenuEntity saved = entityCaptor.getValue();
        assertEquals(MenuTargetTypeEnum.EXTERNAL_LINK, saved.getTargetType());
        assertEquals("http://internal.example.test/home", saved.getExternalUrl());
        assertEquals(ExternalOpenModeEnum.IFRAME, saved.getExternalOpenMode());
        assertNull(saved.getPath());
        assertNull(saved.getComponent());
    }

    @Test
    void externalLinkMenuRejectsExecutableProtocol() {
        MenuSaveForm form = validExternalLinkForm("javascript:alert(1)");

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).insert(any(MenuEntity.class));
    }

    @Test
    void internalPageMenuRequiresComponent() {
        MenuSaveForm form = validExternalLinkForm("https://example.test");
        form.setTargetType(MenuTargetTypeEnum.INTERNAL_PAGE);
        form.setPath("/sys/base/menu");
        form.setComponent(null);

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).insert(any(MenuEntity.class));
    }

    @Test
    void pageMenuParentMustBeAGroupInTheSameApplication() {
        MenuSaveForm form = new MenuSaveForm();
        form.setNumber("user_management");
        form.setName("用户管理");
        form.setLevel(MenuLevelEnum.PAGE);
        form.setParentId(300L);
        form.setAppId(31L);
        form.setFeatureId(100L);
        form.setPermissionId(200L);
        form.setPath("/sys/base/user");
        MenuEntity otherApplicationGroup = new MenuEntity();
        otherApplicationGroup.setId(300L);
        otherApplicationGroup.setAppId(32L);
        otherApplicationGroup.setLevel(MenuLevelEnum.CATEGORY);
        when(mapper.selectById(300L)).thenReturn(otherApplicationGroup);

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).insert(any(MenuEntity.class));
    }

    @Test
    void categoryMenuCannotBelongToAFeature() {
        MenuSaveForm form = validEditForm();
        form.setId(null);
        form.setFeatureId(100L);

        assertThrows(BizException.class, () -> txService.save(form));
        verify(mapper, never()).insert(any(MenuEntity.class));
    }

    private MenuSaveForm validEditForm() {
        MenuSaveForm form = new MenuSaveForm();
        form.setId(1L);
        form.setNumber("menu_management");
        form.setName("菜单");
        form.setLevel(MenuLevelEnum.CATEGORY);
        form.setAppId(31L);
        form.setPermissionId(200L);
        PermissionEntity permission = new PermissionEntity();
        permission.setId(200L);
        permission.setAppId(31L);
        when(permissionMapper.selectById(200L)).thenReturn(permission);
        return form;
    }

    private MenuSaveForm validExternalLinkForm(String externalUrl) {
        MenuSaveForm form = new MenuSaveForm();
        form.setNumber("external_home");
        form.setName("外部首页");
        form.setLevel(MenuLevelEnum.PAGE);
        form.setParentId(0L);
        form.setAppId(31L);
        form.setFeatureId(100L);
        form.setPermissionId(200L);
        form.setTargetType(MenuTargetTypeEnum.EXTERNAL_LINK);
        form.setExternalUrl(externalUrl);
        form.setExternalOpenMode(ExternalOpenModeEnum.IFRAME);
        FeatureEntity feature = new FeatureEntity();
        feature.setId(100L);
        feature.setAppId(31L);
        PermissionEntity permission = new PermissionEntity();
        permission.setId(200L);
        permission.setFeatureId(100L);
        when(featureMapper.selectById(100L)).thenReturn(feature);
        when(permissionMapper.selectById(200L)).thenReturn(permission);
        return form;
    }
}
