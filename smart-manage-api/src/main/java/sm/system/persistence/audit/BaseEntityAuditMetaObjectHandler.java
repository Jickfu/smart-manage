package sm.system.persistence.audit;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import sm.system.entity.BaseEntity;
import sm.system.security.context.CurrentOperatorProvider;

import java.time.LocalDateTime;

/**
 * 统一填充实体审计字段。
 *
 * @author Chekfu
 */
@Component
@RequiredArgsConstructor
public class BaseEntityAuditMetaObjectHandler implements MetaObjectHandler {

    private final CurrentOperatorProvider currentOperatorProvider;

    @Override
    public void insertFill(MetaObject metaObject) {
        // 仅对 BaseEntity 子类填充审计字段，避免误影响其他实体。
        if (!(metaObject.getOriginalObject() instanceof BaseEntity)) {
            return;
        }
        setFieldValByName("createTime", LocalDateTime.now(), metaObject);
        setFieldValByName("createUser", currentOperatorProvider.getCurrentUserIdOrNull(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (!(metaObject.getOriginalObject() instanceof BaseEntity)) {
            return;
        }
        // 更新操作必须覆盖旧审计值，不能使用仅在字段为空时生效的 strictUpdateFill。
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        setFieldValByName("updateUser", currentOperatorProvider.getCurrentUserIdOrNull(), metaObject);
    }
}
