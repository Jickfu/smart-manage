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
  it('完整发行读取装配清单，新增和移除领域无需修改 CI', async () => {
    const root = await fixture();
    const manifest = join(root, 'domains.json');
    for (const domains of [['sys', 'demo'], ['sys', 'demo', 'orders'], ['sys']]) {
      await writeFile(manifest, JSON.stringify(domains));
      expect(selectedDomains('all', manifest)).toEqual(domains);
    }
    for (const invalid of [
      {},
      ['sys', 'sys'],
      ['sys', '../orders'],
      ['sys', 'common'],
      ['sys', null],
    ]) {
      await writeFile(manifest, JSON.stringify(invalid));
      expect(() => selectedDomains('all', manifest)).toThrow();
    }
    await writeFile(manifest, JSON.stringify(['sys', 'missing']));
    await expect(generateRegistry(root, selectedDomains('all', manifest))).rejects.toThrow();
  });
  it('要求平台且拒绝空值、重复和路径穿越', () => {
    expect(selectedDomains('sys,demo')).toEqual(['sys', 'demo']);
    for (const value of ['', 'demo', 'sys,sys', 'sys,../demo'])
      expect(() => selectedDomains(value)).toThrow();
  });
  it('仅生成选中领域，两次生成稳定，删除可选领域后默认仍生成', async () => {
    const root = await fixture();
    await generateRegistry(root, ['sys', 'demo']);
    const both = await generated(root);
    expect(both.every((content) => content.includes('../../demo/'))).toBe(true);
    await generateRegistry(root, ['sys', 'demo']);
    expect(await generated(root)).toEqual(both);
    await rm(join(root, 'src/domain/demo'), { recursive: true });
    await generateRegistry(root, ['sys']);
    expect((await generated(root)).every((content) => !content.includes('demo'))).toBe(true);
    await expect(generateRegistry(root, ['sys', 'demo'])).rejects.toThrow();
  });
});
