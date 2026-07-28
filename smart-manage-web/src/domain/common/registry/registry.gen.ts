/**
 * 页面注册清单导入文件，由 pnpm gen:registry 自动生成，禁止手动修改。
 */

// src/domain/scm/procurement/purchaseRequisition/pageRegistration.ts
import pageRegistrationModule1 from '../../scm/procurement/purchaseRequisition/pageRegistration';

// src/domain/sys/app/pageRegistration.ts
import pageRegistrationModule2 from '../../sys/app/pageRegistration';

// src/domain/sys/cloud/pageRegistration.ts
import pageRegistrationModule3 from '../../sys/cloud/pageRegistration';

// src/domain/sys/menu/pageRegistration.ts
import pageRegistrationModule4 from '../../sys/menu/pageRegistration';

// src/domain/sys/permission/pageRegistration.ts
import pageRegistrationModule5 from '../../sys/permission/pageRegistration';

// src/domain/sys/role/pageRegistration.ts
import pageRegistrationModule6 from '../../sys/role/pageRegistration';

// src/domain/sys/user/pageRegistration.ts
import pageRegistrationModule7 from '../../sys/user/pageRegistration';

import { registerPageRegistrationModules } from './componentRegistry';

registerPageRegistrationModules([
  pageRegistrationModule1,
  pageRegistrationModule2,
  pageRegistrationModule3,
  pageRegistrationModule4,
  pageRegistrationModule5,
  pageRegistrationModule6,
  pageRegistrationModule7,
]);
