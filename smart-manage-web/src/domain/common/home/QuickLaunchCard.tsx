import { useMemo, useState } from 'react';
import { getBlockingQueryError } from '@/api/queryErrorFeedback';
import { RequestErrorState } from '@/domain/common/component/RequestErrorState';
import type { Key } from 'react';
import { AppstoreOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { Button, Card, Empty, Input, Spin, Tree } from 'antd';
import type { TreeDataNode, TreeProps } from 'antd';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import AppModal from '@/domain/common/component/AppModal';
import { useOperationFeedback } from '@/domain/common/component/useOperationFeedback';
import { resolveIcon } from '@/domain/common/component/iconResolver';
import { quickLaunchApi, quickLaunchQueryKeys } from '@/domain/sys/base/user/quickLaunchApi';
import type {
  HomeQuickLaunchScope,
  QuickLaunchItemVO,
  QuickLaunchScopeForm,
} from '@/domain/sys/base/user/quickLaunchApi';
import { openMenuItem } from '@/services/navigationService';
import './QuickLaunchCard.css';

interface QuickLaunchCardProps {
  scope: HomeQuickLaunchScope;
  appNumber?: string;
}

const menuKey = (menuId: string) => `menu:${menuId}`;

function buildTreeData(options: QuickLaunchItemVO[], keyword: string): TreeDataNode[] {
  const normalizedKeyword = keyword.trim().toLocaleLowerCase();
  const filteredOptions = normalizedKeyword
    ? options.filter((option) =>
        [option.name, option.appName, option.groupName]
          .filter(Boolean)
          .some((text) => text?.toLocaleLowerCase().includes(normalizedKeyword)),
      )
    : options;
  const applicationGroups = new Map<string, Map<string, QuickLaunchItemVO[]>>();
  for (const option of filteredOptions) {
    const groupMap = applicationGroups.get(option.appNumber) ?? new Map();
    const groupName = option.groupName || '应用菜单';
    groupMap.set(groupName, [...(groupMap.get(groupName) ?? []), option]);
    applicationGroups.set(option.appNumber, groupMap);
  }
  return [...applicationGroups.entries()].map(([applicationNumber, groupMap]) => {
    const applicationOptions = [...groupMap.values()].flat();
    return {
      key: `app:${applicationNumber}`,
      title: applicationOptions[0]?.appName || applicationNumber,
      children: [...groupMap.entries()].map(([groupName, groupOptions]) => ({
        key: `group:${applicationNumber}:${groupName}`,
        title: groupName,
        children: groupOptions.map((option) => ({
          key: menuKey(option.menuId),
          title: option.name,
          isLeaf: true,
        })),
      })),
    };
  });
}

const QuickLaunchCard = ({ scope, appNumber }: QuickLaunchCardProps) => {
  const feedback = useOperationFeedback();
  const queryClient = useQueryClient();
  const [managerOpen, setManagerOpen] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [selectedMenuIds, setSelectedMenuIds] = useState<string[] | null>(null);
  const scopeForm = useMemo<QuickLaunchScopeForm>(
    () => ({ scope, ...(appNumber ? { appNumber } : {}) }),
    [appNumber, scope],
  );
  const listQuery = useQuery({
    queryKey: quickLaunchQueryKeys.list(scopeForm),
    queryFn: () => quickLaunchApi.list(scopeForm),
  });
  const configurationQuery = useQuery({
    meta: { errorPresentation: 'local-initial' },
    queryKey: quickLaunchQueryKeys.configuration(scopeForm),
    queryFn: () => quickLaunchApi.configuration(scopeForm),
    enabled: managerOpen,
  });
  const saveMutation = useMutation({
    mutationFn: () =>
      quickLaunchApi.save({
        ...scopeForm,
        menuIds: selectedMenuIds ?? configurationQuery.data?.selectedMenuIds ?? [],
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: quickLaunchQueryKeys.all });
      setManagerOpen(false);
      feedback.success('快速发起已更新');
    },
    onError: (error) => feedback.fromError(error, '快速发起保存失败'),
  });

  const treeData = useMemo(
    () => buildTreeData(configurationQuery.data?.options ?? [], keyword),
    [configurationQuery.data?.options, keyword],
  );
  const optionMenuIds = useMemo(
    () => new Set((configurationQuery.data?.options ?? []).map((option) => option.menuId)),
    [configurationQuery.data?.options],
  );
  const checkedKeys = (selectedMenuIds ?? configurationQuery.data?.selectedMenuIds ?? []).map(
    menuKey,
  );

  const handleCheck: TreeProps['onCheck'] = (nextCheckedKeys) => {
    const keys = Array.isArray(nextCheckedKeys) ? nextCheckedKeys : nextCheckedKeys.checked;
    setSelectedMenuIds(
      keys
        .map((key) => String(key))
        .filter((key) => key.startsWith('menu:'))
        .map((key) => key.slice('menu:'.length))
        .filter((menuId) => optionMenuIds.has(menuId)),
    );
  };

  const handleLaunch = async (item: QuickLaunchItemVO) => {
    try {
      await openMenuItem(item.appNumber, {
        id: item.menuId,
        number: item.menuNumber,
        name: item.name,
        icon: item.icon ?? '',
        level: 1,
        routes: [],
        component: item.component,
        targetType: item.targetType,
        externalUrl: item.externalUrl,
        externalOpenMode: item.externalOpenMode,
      });
    } catch (error) {
      feedback.fromError(error, '快捷入口打开失败');
    }
  };

  return (
    <>
      <Card className="sm-home-card sm-quick-launch-card" title="快速发起" variant="borderless">
        <Spin spinning={listQuery.isLoading}>
          <div className="sm-quick-launch-scroll">
            <div className="sm-quick-launch-grid">
              {(listQuery.data ?? []).map((item) => (
                <button
                  key={item.menuId}
                  type="button"
                  className="sm-quick-launch-item"
                  title={`${item.appName} · ${item.name}`}
                  onClick={() => void handleLaunch(item)}
                >
                  <span className="sm-quick-launch-icon">
                    {resolveIcon(item.icon, <AppstoreOutlined />)}
                  </span>
                  <span className="sm-quick-launch-name">{item.name}</span>
                </button>
              ))}
              <button
                type="button"
                className="sm-quick-launch-item sm-quick-launch-item--add"
                title="管理快速发起"
                onClick={() => {
                  setKeyword('');
                  setSelectedMenuIds(null);
                  setManagerOpen(true);
                }}
              >
                <span className="sm-quick-launch-icon">
                  <PlusOutlined />
                </span>
                <span className="sm-quick-launch-name">添加</span>
              </button>
            </div>
          </div>
        </Spin>
      </Card>
      <AppModal
        title="快速发起管理"
        open={managerOpen}
        width={820}
        onCancel={() => setManagerOpen(false)}
        headerExtra={
          <Input
            className="sm-quick-launch-search"
            allowClear
            prefix={<SearchOutlined />}
            placeholder="搜索应用或菜单"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
        }
        footer={
          <>
            <Button onClick={() => setManagerOpen(false)}>取消</Button>
            <Button
              type="primary"
              loading={saveMutation.isPending}
              disabled={
                configurationQuery.isLoading || Boolean(getBlockingQueryError(configurationQuery))
              }
              onClick={() => {
                if (!configurationQuery.isLoading && !getBlockingQueryError(configurationQuery))
                  saveMutation.mutate();
              }}
            >
              确定
            </Button>
          </>
        }
      >
        <div className="sm-quick-launch-manager-content">
          <Spin spinning={configurationQuery.isLoading}>
            {getBlockingQueryError(configurationQuery) && (
              <RequestErrorState
                error={configurationQuery.error}
                onRetry={() => void configurationQuery.refetch()}
              />
            )}
            <div
              hidden={Boolean(getBlockingQueryError(configurationQuery))}
              inert={Boolean(getBlockingQueryError(configurationQuery))}
            >
              {treeData.length ? (
                <Tree
                  className="sm-quick-launch-tree"
                  blockNode
                  checkable
                  defaultExpandAll
                  selectable={false}
                  checkedKeys={checkedKeys as Key[]}
                  treeData={treeData}
                  onCheck={handleCheck}
                />
              ) : (
                <Empty description={keyword ? '没有匹配的菜单' : '当前范围内暂无可用菜单'} />
              )}
            </div>
          </Spin>
        </div>
      </AppModal>
    </>
  );
};

export default QuickLaunchCard;
