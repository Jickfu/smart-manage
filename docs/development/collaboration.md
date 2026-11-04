# 协作与质量

面向程序员与 Agent 的任务导航。协作规则由[根 AGENTS](../../AGENTS.md)及所属子项目 AGENTS 维护，本文不复制协作条款或建立第二套工作流程。

| 任务 | 阅读入口 |
| --- | --- |
| 前端设计与桌面页面 | [前端规范](../design/README.md)、[前端架构](../architecture/frontend.md)、[页面指南](./frontend-page-guide.md) |
| 后端实现 | [后端规范](../backend/README.md)、[平台 AGENTS](../../smart-manage-server/platform/AGENTS.md)及所属模块要求 |
| 移动端设计与接入 | [移动端规范](../mobile/README.md) |
| 新增或显著扩展模块 | [模块开发指南](./module-development-guide.md)、[模块样板](./module-pattern-catalog.md) |
| 新增业务聚合 | [业务聚合检查](./business-aggregate-checklist.md) |
| 安全、权限及隐私 | [安全架构](../architecture/security.md)、[数据权限](../architecture/data-permission.md)、[数据脱敏](../architecture/data-masking.md) |
| 验证与交付 | [质量验证](./verification.md) |

项目 skill 编排上述文档，不拥有另一份架构或设计规则。按任务加载相关正文，避免无差别读取全部文档。

## 代表任务路径

以下路径用于找到任务要求，不规定统一文件模板或额外报告。需求边界与验收结论可直接在当前任务中说明；只有长期契约需要变化时才修改其权威正文。

| 代表任务 | 阅读顺序 | 完成判据所在正文 |
| --- | --- | --- |
| 新增桌面列表与编辑页 | 根及桌面 AGENTS → 前端架构 → 前端规范及适配边界 → 页面指南的页面形态、列表和编辑章节 → 样板目录；涉及新增功能身份时补读功能与权限及页面注册协议 | [页面指南](./frontend-page-guide.md)定义页面接入，[质量验证](./verification.md)定义前端、注册生成和浏览器验证；属于新模块或显著扩展时再进入模块指南 |
| 扩展后端业务模块 | 根及所属目录 AGENTS → 后端总览 → 后端架构与所属模块文档 → 模块指南及相关后端样板；按实际能力补读安全、数据权限、数据库或状态约束 | [模块指南](./module-development-guide.md#6-完成前复核)定义接入复核，[质量验证](./verification.md)定义测试及真实数据库验证的触发条件；纯后端任务不要求新增桌面页面 |
| 规划移动查询页 | 根 AGENTS → 移动端规范 → 适配边界 → 实际接口的后端、安全及数据权限契约 | [移动端规范](../mobile/README.md#查询页规划)定义规划完成标准；尚无客户端工程时不宣称完成运行、真机或平台兼容验收 |

三条路径都回到已有权威正文。设计建议、样板代码和静态门禁不能替代产品边界、真实权限或交互验收。
