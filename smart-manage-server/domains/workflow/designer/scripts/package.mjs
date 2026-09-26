import { spawnSync } from 'node:child_process';
import { cpSync, mkdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../', import.meta.url));
const tests = spawnSync(process.execPath, ['--test', 'src/protocol.test.mjs'], { cwd: root, stdio: 'inherit' });
if (tests.status !== 0) process.exit(tests.status ?? 1);
for (const args of [['install', '--frozen-lockfile'], ['build']]) {
  const result = spawnSync('pnpm', args, { cwd: root, stdio: 'inherit', shell: process.platform === 'win32' });
  if (result.error) throw result.error;
  if (result.status !== 0) process.exit(result.status ?? 1);
}
const destination = new URL('../../target/classes/static/workflow/designer/', import.meta.url);
mkdirSync(destination, { recursive: true });
cpSync(new URL('../dist/', import.meta.url), destination, { recursive: true });
cpSync(new URL('../target/upstream/LICENSE', import.meta.url), new URL('WARM-FLOW-LICENSE.txt', destination));
