import type { WorkflowBusinessChoice } from '../api';

export const workflowBusinessQueryKey = ['workflow', 'business-types'] as const;

export interface WorkflowBusinessTreeNode extends Record<string, unknown> {
  key: string;
  title: string;
  isLeaf?: boolean;
  children?: WorkflowBusinessTreeNode[];
}

/** 同一目录转换同时服务流程定义列表和业务参照，节点身份始终使用目录 ID。 */
export function createWorkflowBusinessTree(
  choices: WorkflowBusinessChoice[],
  rootTitle: string,
): WorkflowBusinessTreeNode[] {
  const domains = new Map<
    string,
    WorkflowBusinessTreeNode & { children: WorkflowBusinessTreeNode[] }
  >();
  for (const choice of choices) {
    const domainKey = `domain:${choice.domainId}`;
    const domain = domains.get(domainKey) ?? {
      key: domainKey,
      title: choice.domainName,
      children: [],
    };
    const appKey = `app:${choice.appId}`;
    if (!domain.children.some((application) => application.key === appKey)) {
      domain.children.push({ key: appKey, title: choice.appName, isLeaf: true });
    }
    domains.set(domainKey, domain);
  }
  return [{ key: 'root', title: rootTitle, children: [...domains.values()] }];
}

export function parseWorkflowBusinessScope(key?: React.Key) {
  if (!key || key === 'root') return {};
  const [scope, id] = String(key).split(':');
  return scope === 'domain' ? { domainId: id } : scope === 'app' ? { appId: id } : {};
}
