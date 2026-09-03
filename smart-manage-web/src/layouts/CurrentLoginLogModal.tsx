import { useState } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { Button, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import AppModal from '@/domain/common/component/AppModal';
import ListTableShell from '@/domain/common/page/list/ListTableShell';
import { loginLogApi } from '@/domain/sys/monitor/loginLog/api';
import type { LoginEventType, LoginLogListVO } from '@/domain/sys/monitor/loginLog/types';

interface CurrentLoginLogModalProps {
  open: boolean;
  onClose: () => void;
}

const EVENT_LABELS: Record<LoginEventType, string> = {
  LOGIN_SUCCESS: '登录成功',
  LOGIN_FAILURE: '登录失败',
  PASSWORD_CHANGE_REQUIRED: '要求修改密码',
  LOGOUT: '退出登录',
  SESSION_KICKED: '会话被踢下线',
  SESSION_REPLACED: '会话被顶替',
  ACCOUNT_DISABLED: '账号禁用',
  PASSWORD_RESET_TERMINATED: '重置密码下线',
  TEMPORARY_LOGIN_GRANT_CREATED: '生成代登录凭证',
  TEMPORARY_LOGIN_SUCCESS: '代登录成功',
};

const COLUMNS: ColumnsType<LoginLogListVO> = [
  {
    title: '发生时间',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: '事件',
    dataIndex: 'eventType',
    width: 150,
    render: (eventType: LoginEventType) => EVENT_LABELS[eventType] ?? eventType,
  },
  {
    title: '结果',
    dataIndex: 'success',
    width: 80,
    render: (success: boolean) =>
      success ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>,
  },
  {
    title: 'IP 地址',
    dataIndex: 'ip',
  },
];

export default function CurrentLoginLogModal({ open, onClose }: CurrentLoginLogModalProps) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const query = useQuery({
    queryKey: ['sys', 'monitor', 'login-log', 'current', pageNum, pageSize],
    queryFn: () => loginLogApi.currentListPage({ pageNum, pageSize }),
    enabled: open,
    placeholderData: keepPreviousData,
  });

  const closeModal = () => {
    setPageNum(1);
    onClose();
  };

  return (
    <AppModal
      title="登录日志（近7天）"
      open={open}
      width={760}
      bodyMode="fixed"
      className="sm-current-login-log-modal sm-current-audit-log-modal"
      onCancel={closeModal}
      footer={<Button onClick={closeModal}>关闭</Button>}
    >
      <ListTableShell
        total={query.data?.total ?? 0}
        pageNum={pageNum}
        pageSize={pageSize}
        onPageChange={(nextPageNum, nextPageSize) => {
          setPageNum(nextPageNum);
          setPageSize(nextPageSize);
        }}
        table={
          <Table
            className="sm-list-table sm-current-login-log-table"
            rowKey="id"
            size="small"
            loading={query.isLoading}
            columns={COLUMNS}
            dataSource={query.data?.records ?? []}
            pagination={false}
            sticky
            scroll={{ x: 620, y: 1 }}
          />
        }
      />
    </AppModal>
  );
}
