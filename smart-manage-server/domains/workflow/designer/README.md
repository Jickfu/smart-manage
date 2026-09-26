# 官方设计器集成

独立 iframe 构建，复用 Warm-Flow v1.8.9 源码的公开设计器及 DataProvider 入口；不修改上游画布和节点实现。源码固定为 `37b17a8939f0931ab8c19d66b768050325da3bcf`。

官方预构建 JAR 默认从 URL/localStorage 读取请求头值，无法直接满足本项目内存 CSRF 和脏数据保护；本包装仅管理数据访问、会话和宿主事件。官方 npm 包在接入检查时返回 404，因此从固定来源构建，不假装存在已发布组件制品。

运行 `pnpm install --frozen-lockfile`、`pnpm build`。上游源码位于忽略的 target/upstream，源码有修改或版本不同即拒绝复用。产物待集成到 workflow 自有资源；本目录当前处于技术验证阶段，未接通生产接口。

上游 LICENSE 及所有依赖的版权声明必须随发布产物保留。此 UI 使用官方 Vue/Element Plus，隔离在 iframe 内，不进入 Smart Manage React 主应用依赖。
