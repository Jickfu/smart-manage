import { createElement, Suspense } from 'react';
import { Button, Empty, Spin } from 'antd';
import { getDomainView } from './domainExtensions';
import type { DomainViewProps } from './domainExtensions';
export function DomainView({ viewKey, ...props }: DomainViewProps & { viewKey: string }) {
  const View = getDomainView(viewKey);
  return View ? (
    <Suspense fallback={<Spin />}>{createElement(View, props)}</Suspense>
  ) : (
    <Empty description="对应业务模块未安装或未提供此页面">
      {props.onBack && <Button onClick={props.onBack}>返回</Button>}
    </Empty>
  );
}
