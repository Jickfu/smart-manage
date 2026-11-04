# 第三方软件说明

Smart Manage 使用以下第三方开源软件。依赖自身的许可证继续适用于对应第三方代码。

| 软件 | 用途 | 版本 | 许可证 | 项目地址 |
| --- | --- | --- | --- | --- |
| tianai-captcha | 滑块拼图生成与轨迹匹配核心 | 1.5.5 | MulanPSL 2.0 | https://github.com/dromara/tianai-captcha |
| Authlete HTTP Message Signatures | RFC 9421 Structured Fields 解析与签名基串构造 | 1.8 | Apache License 2.0 | https://github.com/authlete/http-message-signatures |
| IPAddress | IPv4/IPv6 字面量、CIDR 与子网包含判断 | 5.6.2 | Apache License 2.0 | https://github.com/seancfoley/IPAddress |
| commonmark-java（含 GFM Tables 扩展） | API 接口文档的 CommonMark AST、表格构造与渲染 | 0.29.0 | BSD 2-Clause | https://github.com/commonmark/commonmark-java |
| VitePress | 独立设计文档浏览、静态构建及本地检索 | 1.6.4 | MIT | https://github.com/vuejs/vitepress |
| Vue | 独立文档工具渲染，不进入 React 管理端 | 3.5.24 | MIT | https://github.com/vuejs/core |

完整的传递依赖清单以 Maven 和 pnpm 锁定文件为准。

独立文档工具的图表评审试样使用 Apache ECharts 6.1.0（Apache License 2.0），来源：https://github.com/apache/echarts 。相关依赖只在 tools/design-docs 中维护。
