# 前端页面注册约定

## 定位

每个业务模块或单据目录使用一个 `pageRegistration.ts` 作为模块级页面白名单清单。页面组件仍保持一文件一个页面组件，注册清单可以声明列表、编辑和自定义等多个页面入口。

本文档被[前端架构](./frontend.md)引用：前端架构描述页面注册的上层边界，本文档定义同一机制的具体注册与校验约定。

## 示例

```tsx
export default definePageRegistrations([
  {
    featureKey: 'scm/procurement/purchase-requisition',
    componentKey: 'scm/procurement/purchase-requisition',
    title: '采购申请',
    pageType: 'LIST',
    component: lazy(() => import('./PurchaseRequisitionListPage')),
  },
  {
    featureKey: 'scm/procurement/purchase-requisition',
    componentKey: 'scm/procurement/purchase-requisition/edit',
    title: '采购申请',
    pageType: 'EDIT',
    component: lazy(() => import('./PurchaseRequisitionEditPage')),
  },
]);
```

## 页签标题

- 页签标题由前端页面定义，后端菜单名称只用于菜单展示，不得作为页签标题或标题兜底。
- 每个页面注册项必须声明非空 `title`，内容是页面稳定的基础名称，例如“用户”“应用”“采购申请”。
- `LIST` 页签由工作台统一显示为“基础名称 + 列表”，例如“用户列表”；页面传给 `ListPage` 的 `title` 必须使用同一个基础名称，不得自行包含“列表”。
- `EDIT` 页签只显示基础名称，不得添加“新增”“编辑”“查看”“详情”等操作文字；页面传给 `EditPage` 的 `title` 必须使用同一个基础名称。
- `CUSTOM` 页签直接使用页面声明的标题，不自动添加后缀。直接复用 `EditPageShell` 的 CUSTOM 页面必须显式传入 `title`，由页面壳同步页签标题。
- `ModalEditPage` 的标题只显示基础名称，不得添加“新增”“编辑”“查看”“详情”等操作文字。
- 页面确需根据业务上下文动态命名时，可以通过 `useSetPageTabTitle` 设置非空标题。覆盖只改变显示名称，不得改变 `tabKey`、`componentKey`、单据 ID 或页签复用规则。
- 新增保存后临时页签替换为真实单据页签时，必须恢复目标页面注册的默认标题；页面若需要动态标题，应在替换完成后按自身业务逻辑再次覆盖。

## 生成与校验

- `pnpm gen:registry` 只发现并导入 `src/domain/**/pageRegistration.ts(x)`。
- 生成器不解析组件文件名，不从文件名推导页面键，也不使用正则读取业务声明。
- 页面键、基础标题、页面类型和懒加载组件必须在清单中显式声明。
- 每个页面必须显式声明 `featureKey`；同一功能的多个页面共享稳定功能键。
- 未在清单声明的组件不会进入注册表，后端菜单字符串无法加载任意前端模块。
- 空清单和重复 `componentKey` 在注册阶段直接抛错，不保留兼容逻辑。
