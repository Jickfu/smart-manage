package sm.domain.sys.message.inbox.service;

import org.junit.jupiter.api.Test;
import sm.domain.sys.message.inbox.model.form.InboxMessageSaveForm;
import sm.domain.sys.message.inbox.model.form.InboxMessageVersionForm;
import sm.system.security.authorization.AdministratorOnly;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InboxMessageAdminServiceTests {
    @Test
    void onlyFullSitePublishCommandsRequireRealAdministrator() throws NoSuchMethodException {
        assertTrue(InboxMessageAdminService.class.getMethod("publish", InboxMessageVersionForm.class)
                .isAnnotationPresent(AdministratorOnly.class));
        assertTrue(InboxMessageAdminService.class.getMethod("retry", InboxMessageVersionForm.class)
                .isAnnotationPresent(AdministratorOnly.class));
        assertFalse(InboxMessageAdminService.class.getMethod("save", InboxMessageSaveForm.class)
                .isAnnotationPresent(AdministratorOnly.class));
    }
}
