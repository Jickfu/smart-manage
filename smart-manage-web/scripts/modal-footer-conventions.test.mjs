import { resolve } from 'node:path';
import { describe, expect, it } from 'vitest';
import { inspectModalFooterDirectory, inspectModalFooters } from './modal-footer-conventions.mjs';

const imports = `
import AppModal from '@/domain/common/component/AppModal';
import { Button, Space, Flex } from 'antd';
import { PermissionActions } from '@/domain/common/page/access/PermissionActions';
`;
const inspect = (source) => inspectModalFooters(imports + source);

describe('AppModal 页脚布局契约', () => {
  it('当前仓库满足页脚约定', () => {
    expect(inspectModalFooterDirectory(resolve(import.meta.dirname, '../src'))).toEqual([]);
  });
  it.each(['Space', 'Flex', 'div'])('拒绝 %s 包裹按钮，包括条件分支', (wrapper) => {
    expect(
      inspect(`<AppModal footer={<${wrapper}>{ready && <Button>确定</Button>}</${wrapper}>} />`),
    ).toHaveLength(1);
  });
  it('允许 Fragment、数组、条件按钮及显式关闭分组', () => {
    expect(
      inspect(`
      <AppModal footer={<><Button>关闭</Button>{ready ? <Button>复制</Button> : null}</>} />;
      <AppModal footer={[<Button key="close">关闭</Button>]} />;
      <AppModal footer={<PermissionActions {...props} grouped={false} />} />;
      <AppModal footer={null} />;
    `),
    ).toEqual([]);
  });
  it.each(['', 'grouped', 'grouped={true}', 'grouped={flag}', 'grouped={false} {...props}'])(
    '拒绝不能保证关闭分组的 PermissionActions：%s',
    (attributes) => {
      expect(inspect(`<AppModal footer={<PermissionActions ${attributes} />} />`)).toHaveLength(1);
    },
  );
  it('追踪同作用域页脚变量和导入别名', () => {
    expect(
      inspectModalFooters(`
      import Dialog from './AppModal';
      import { Space as Group, Button as Action } from 'antd';
      const footer = (<Group><Action>确定</Action></Group>);
      <Dialog footer={footer} />;
    `),
    ).toHaveLength(1);
  });
  it('同名变量按作用域解析，循环变量不会无限递归', () => {
    expect(
      inspect(`
      const footer = <Space><Button>外层</Button></Space>;
      function Example() { const footer = <Button>内层</Button>; return <AppModal footer={footer} />; }
      const cycle = cycle;
      <AppModal footer={cycle} />;
    `),
    ).toEqual([]);
  });
  it('不误报正文、其他组件页脚、事件回调或注释', () => {
    expect(
      inspect(`
      // <AppModal footer={<Space><Button /></Space>} />
      <Other footer={<Space><Button /></Space>} />;
      <AppModal footer={<Button onClick={() => <Space><Button /></Space>} />}>
        <Space><Button /></Space>
      </AppModal>;
    `),
    ).toEqual([]);
  });
});
