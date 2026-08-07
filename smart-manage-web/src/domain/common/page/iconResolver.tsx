import {
  HomeOutlined,
  AppstoreOutlined,
  SettingOutlined,
  UserOutlined,
  TeamOutlined,
  CloudOutlined,
  DatabaseOutlined,
  FileOutlined,
  FolderOutlined,
  MonitorOutlined,
  SafetyOutlined,
  AuditOutlined,
  ToolOutlined,
  DashboardOutlined,
  BarChartOutlined,
  PieChartOutlined,
  LineChartOutlined,
  TableOutlined,
  FormOutlined,
  ProfileOutlined,
  UnorderedListOutlined,
  SearchOutlined,
  FilterOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  DownloadOutlined,
  UploadOutlined,
  ExportOutlined,
  ImportOutlined,
  CheckOutlined,
  CloseOutlined,
  LockOutlined,
  UnlockOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
  MailOutlined,
  PhoneOutlined,
  MessageOutlined,
  NotificationOutlined,
  BellOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  GlobalOutlined,
  LinkOutlined,
  MenuOutlined,
  ApartmentOutlined,
  BranchesOutlined,
  NodeIndexOutlined,
  DeploymentUnitOutlined,
} from '@ant-design/icons';
import type { ComponentType, ReactNode } from 'react';
import AsyncIcon from '@/domain/common/component/AsyncIcon';

/** 常用图标白名单 — 覆盖菜单和页面常用场景，按需扩展 */
const iconMap: Record<string, ComponentType> = {
  HomeOutlined,
  AppstoreOutlined,
  SettingOutlined,
  UserOutlined,
  TeamOutlined,
  CloudOutlined,
  DatabaseOutlined,
  FileOutlined,
  FolderOutlined,
  MonitorOutlined,
  SafetyOutlined,
  AuditOutlined,
  ToolOutlined,
  DashboardOutlined,
  BarChartOutlined,
  PieChartOutlined,
  LineChartOutlined,
  TableOutlined,
  FormOutlined,
  ProfileOutlined,
  UnorderedListOutlined,
  SearchOutlined,
  FilterOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  DownloadOutlined,
  UploadOutlined,
  ExportOutlined,
  ImportOutlined,
  CheckOutlined,
  CloseOutlined,
  LockOutlined,
  UnlockOutlined,
  EyeOutlined,
  EyeInvisibleOutlined,
  MailOutlined,
  PhoneOutlined,
  MessageOutlined,
  NotificationOutlined,
  BellOutlined,
  CalendarOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  GlobalOutlined,
  LinkOutlined,
  MenuOutlined,
  ApartmentOutlined,
  BranchesOutlined,
  NodeIndexOutlined,
  DeploymentUnitOutlined,
};

/** Ant Design 对外暴露的图标组件命名规则。 */
export function isSelectableIconName(name: string): boolean {
  return /(?:Outlined|Filled|TwoTone)$/.test(name);
}

/** 根据图标名称解析组件；常用图标同步返回，其余图标按需补载。 */
export function resolveIcon(name: string | undefined): ReactNode | undefined {
  if (!name) return undefined;
  const IconComponent = iconMap[name];
  return IconComponent ? <IconComponent /> : <AsyncIcon name={name} loadIcons={loadAllIcons} />;
}

/** 全量图标映射（懒加载）— 供图标选择器使用，主 bundle 不包含 */
let allIconsPromise: Promise<Record<string, ComponentType>> | undefined;

export async function loadAllIcons(): Promise<Record<string, ComponentType>> {
  allIconsPromise ??= import('@ant-design/icons')
    .then(
      (icons) =>
        Object.fromEntries(
          Object.entries(icons).filter(([name]) => isSelectableIconName(name)),
        ) as Record<string, ComponentType>,
    )
    .catch((error: unknown) => {
      allIconsPromise = undefined;
      throw error;
    });
  return allIconsPromise;
}
