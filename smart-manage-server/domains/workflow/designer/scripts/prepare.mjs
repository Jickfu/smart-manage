import { mkdirSync, existsSync } from 'node:fs';
import { resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';

const projectRoot = fileURLToPath(new URL('../', import.meta.url));
const upstreamRoot = resolve(projectRoot, 'target/upstream');
const revision = '37b17a8939f0931ab8c19d66b768050325da3bcf';
mkdirSync(upstreamRoot, { recursive: true });
function git(argumentsList) {
  return execFileSync('git', ['-C', upstreamRoot, ...argumentsList], { encoding: 'utf8' }).trim();
}
// 不重置已有工作区；来源或版本不符合预期时中止，避免悄悄跟随上游分支。
if (!existsSync(resolve(upstreamRoot, '.git'))) {
  git(['init']);
  git(['config', 'core.longpaths', 'true']);
  git(['sparse-checkout', 'init', '--cone']);
  git(['sparse-checkout', 'set', 'warm-flow-vue-designer']);
  git(['remote', 'add', 'origin', 'https://github.com/dromara/warm-flow.git']);
  git(['fetch', '--depth=1', 'origin', revision]);
  git(['checkout', '--detach', revision]);
}
if (git(['rev-parse', 'HEAD']) !== revision || git(['status', '--porcelain']) !== '') {
  throw new Error('设计器上游源码必须为锁定版本且无本地修改');
}
console.log('Warm-Flow designer source verified: ' + revision);
