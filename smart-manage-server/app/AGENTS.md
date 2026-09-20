# 可执行应用模块规则

本目录继承根目录 `AGENTS.md`，开发时同时遵守[平台后端规则](../platform/AGENTS.md)。

本模块是唯一可运行的组合根，只维护启动类、运行配置、发行装配和装配级测试；禁止在此放置平台或业务领域实现。根 `pom.xml` 仅负责父配置与 reactor 构建，不承担运行职责。

架构以[后端架构](../../docs/architecture/backend.md)为准；新增或显著扩展模块先阅读[模块开发指南](../../docs/development/module-development-guide.md)。验证按[质量验证](../../docs/development/verification.md)执行。
