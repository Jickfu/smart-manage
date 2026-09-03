import { Button, Dropdown } from 'antd';
import type { MenuProps } from 'antd';
import { usePermissionAccess } from '@/domain/common/page/access/usePermissionAccess';

export type DataExportLayout = 'EXPORT_TEMPLATE' | 'IMPORT_TEMPLATE';

interface DataExchangeActionsProps {
  permissionPrefix: string;
  importPermission?: string;
  exportPermission: string;
  exporting?: boolean;
  onImport?: () => void;
  onExport: (layout: DataExportLayout) => void;
}

/** 数据交换操作组；领域页面只提供命令和权限，不重复组合按钮结构。 */
export const DataExchangeActions = ({
  permissionPrefix,
  importPermission,
  exportPermission,
  exporting,
  onImport,
  onExport,
}: DataExchangeActionsProps) => {
  const { can } = usePermissionAccess(permissionPrefix);
  const showImport = Boolean(onImport && can(importPermission));
  const showExport = can(exportPermission);
  if (!showImport && !showExport) return null;

  if (!showExport) {
    return (
      <Button type="primary" onClick={onImport}>
        导入
      </Button>
    );
  }

  const exportItems: MenuProps['items'] = [
    { key: 'EXPORT_TEMPLATE', label: '按导出模板导出' },
    { key: 'IMPORT_TEMPLATE', label: '按导入模板导出' },
  ];
  const handleExportMenuClick: MenuProps['onClick'] = ({ key }) => {
    onExport(key as DataExportLayout);
  };

  if (showImport) {
    return (
      <Dropdown.Button
        type="primary"
        loading={exporting}
        menu={{ items: exportItems, onClick: handleExportMenuClick }}
        onClick={onImport}
      >
        导入
      </Dropdown.Button>
    );
  }

  return (
    <Dropdown.Button
      type="primary"
      loading={exporting}
      menu={{ items: exportItems.slice(1), onClick: handleExportMenuClick }}
      onClick={() => onExport('EXPORT_TEMPLATE')}
    >
      按导出模板导出
    </Dropdown.Button>
  );
};
