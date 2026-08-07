package sm.domain.sys.base.menu.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.base.common.enums.MenuLevelEnum;
import sm.domain.sys.base.common.helper.CurrentUserContext;
import sm.domain.sys.base.menu.mapper.MenuMapper;
import sm.domain.sys.base.menu.model.entity.MenuEntity;
import sm.domain.sys.base.menu.model.form.MenuSaveForm;
import sm.system.exception.BizException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuTxServiceTests {
    private final CurrentUserContext currentUserContext = mock(CurrentUserContext.class);
    private final MenuMapper mapper = mock(MenuMapper.class);
    private final MenuTxService txService = new MenuTxService(currentUserContext, mapper);

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
    void menuLevelsUseZeroAndOne() {
        assertEquals(0, MenuLevelEnum.CATEGORY.getCode());
        assertEquals(1, MenuLevelEnum.PAGE.getCode());
    }

    private MenuSaveForm validEditForm() {
        MenuSaveForm form = new MenuSaveForm();
        form.setId(1L);
        form.setNumber("menu_management");
        form.setName("菜单");
        form.setLevel(MenuLevelEnum.CATEGORY);
        form.setAppId(31L);
        return form;
    }
}
