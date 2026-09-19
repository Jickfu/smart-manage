package sm.system.persistence.audit;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;
import sm.system.entity.BaseEntity;
import sm.system.security.context.CurrentOperatorProvider;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseEntityAuditMetaObjectHandlerTests {

    private final CurrentOperatorProvider currentOperatorProvider = mock(CurrentOperatorProvider.class);
    private final BaseEntityAuditMetaObjectHandler handler =
            new BaseEntityAuditMetaObjectHandler(currentOperatorProvider);

    @Test
    void fillsCreateFieldsOnlyForBaseEntity() {
        when(currentOperatorProvider.getCurrentUserIdOrNull()).thenReturn(11L);
        BaseEntity entity = new BaseEntity();

        handler.insertFill(SystemMetaObject.forObject(entity));

        assertNotNull(entity.getCreateTime());
        assertEquals(11L, entity.getCreateUser());
        assertNull(entity.getUpdateTime());
        assertNull(entity.getUpdateUser());
    }

    @Test
    void updateOverwritesExistingAuditFields() {
        when(currentOperatorProvider.getCurrentUserIdOrNull()).thenReturn(22L);
        BaseEntity entity = new BaseEntity();
        LocalDateTime previousTime = LocalDateTime.of(2020, 1, 1, 0, 0);
        entity.setUpdateTime(previousTime);
        entity.setUpdateUser(1L);

        handler.updateFill(SystemMetaObject.forObject(entity));

        assertNotNull(entity.getUpdateTime());
        assertNotEquals(previousTime, entity.getUpdateTime());
        assertEquals(22L, entity.getUpdateUser());
    }

    @Test
    void ignoresObjectsOutsideBaseEntityHierarchy() {
        PlainObject plainObject = new PlainObject();
        MetaObject metaObject = SystemMetaObject.forObject(plainObject);

        handler.insertFill(metaObject);
        handler.updateFill(metaObject);

        assertNull(plainObject.createTime);
        assertNull(plainObject.updateTime);
    }

    private static final class PlainObject {
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
}
