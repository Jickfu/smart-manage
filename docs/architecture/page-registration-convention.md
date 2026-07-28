# 前端页面注册约定

## 定位

每个业务模块或单据目录使用一个 `pageRegistration.ts` 作为模块级页面白名单清单。页面组件仍保持一文件一个页面组件，注册清单可以声明列表、编辑和自定义等多个页面入口。

本文档被[前端架构](./frontend.md)引用：前端架构描述页面注册的上层边界，本文档定义同一机制的具体注册与校验约定。

## 示例

```tsx
export default definePageRegistrations([
  {
    componentKey: 'scm/procurement/purchase-requisition',
    pageType: 'LIST',
    component: lazy(() => import('./PurchaseRequisitionListPage')),
  },
  {
    componentKey: 'scm/procurement/purchase-requisition/edit',
    pageType: 'EDIT',
    component: lazy(() => import('./PurchaseRequisitionEditPage')),
  },
]);
```

## 生成与校验

- `pnpm gen:registry` 只发现并导入 `src/domain/**/pageRegistration.ts(x)`。
- 生成器不解析组件文件名，不从文件名推导页面键，也不使用正则读取业务声明。
- 页面键、页面类型和懒加载组件必须在清单中显式声明。
- 未在清单声明的组件不会进入注册表，后端菜单字符串无法加载任意前端模块。
- 空清单和重复 `componentKey` 在注册阶段直接抛错，不保留兼容逻辑。
