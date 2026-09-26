// @vitest-environment node
import { afterEach, describe, expect, it } from 'vitest';
import { mkdtemp, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { selectedDomains } from './selected-domains.mjs';
import { generateRegistry } from './gen-registry.mjs';

const directories = [];
afterEach(async () => {
  for (const directory of directories.splice(0))
    await rm(directory, { recursive: true, force: true });
});
async function fixture() {
  const root = await mkdtemp(join(tmpdir(), 'smart-manage-registry-'));
  directories.push(root);
  await mkdir(join(root, 'src/domain/common/registry'), { recursive: true });
  for (const domain of ['sys', 'demo']) {
    const directory = join(root, 'src/domain', domain);
    await mkdir(join(directory, 'application'), { recursive: true });
    await writeFile(join(directory, 'application/pageRegistration.ts'), 'export default [];');
    await writeFile(join(directory, 'applicationHomes.ts'), 'export default [];');
  }
  return root;
}
async function generated(root) {
  return Promise.all(
    ['registry.gen.ts', 'applicationHomes.gen.ts'].map((file) =>
      readFile(join(root, 'src/domain/common/registry', file), 'utf8'),
    ),
  );
}

describe('领域构建选择', () => {
  it('默认发现当前领域目录，新增和移除领域无需修改 CI', async () => {
    const root = await fixture();
    const domainRoot = join(root, 'src/domain');
    expect(selectedDomains(undefined, domainRoot)).toEqual(['sys', 'demo']);
    expect(selectedDomains('all', domainRoot)).toEqual(['sys', 'demo']);
    await mkdir(join(domainRoot, 'orders'));
    expect(selectedDomains(undefined, domainRoot)).toEqual(['sys', 'demo', 'orders']);
    await rm(join(domainRoot, 'demo'), { recursive: true });
    expect(selectedDomains(undefined, domainRoot)).toEqual(['sys', 'orders']);
  });
  it('要求平台且拒绝空值、重复和路径穿越', () => {
    expect(selectedDomains('sys')).toEqual(['sys']);
    for (const value of ['', 'demo', 'sys,sys', 'sys,../demo'])
      expect(() => selectedDomains(value)).toThrow();
  });
  it('显式声明的领域依赖不可被单独裁剪，平台选择不受可选依赖影响', async () => {
    const root = await fixture();
    const domainRoot = join(root, 'src/domain');
    await writeFile(join(domainRoot, 'demo/dependencies.json'), '["workflow"]');
    expect(() => selectedDomains('all', domainRoot)).toThrow('workflow');
    expect(selectedDomains('sys', domainRoot)).toEqual(['sys']);
    await mkdir(join(domainRoot, 'workflow'));
    expect(selectedDomains('all', domainRoot)).toEqual(['sys', 'demo', 'workflow']);
    expect(() => selectedDomains('sys,demo', domainRoot)).toThrow('workflow');
  });
  it('仅生成选中领域，两次生成稳定，删除可选领域后默认自动收敛', async () => {
    const root = await fixture();
    await generateRegistry(root, ['sys', 'demo']);
    const both = await generated(root);
    expect(both.every((content) => content.includes('../../demo/'))).toBe(true);
    await generateRegistry(root, ['sys', 'demo']);
    expect(await generated(root)).toEqual(both);
    await rm(join(root, 'src/domain/demo'), { recursive: true });
    await generateRegistry(root, selectedDomains(undefined, join(root, 'src/domain')));
    expect((await generated(root)).every((content) => !content.includes('demo'))).toBe(true);
    await expect(generateRegistry(root, ['sys', 'demo'])).rejects.toThrow();
  });
});
