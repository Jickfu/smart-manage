import { useState } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { Button, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import AppModal from '@/domain/common/component/AppModal';
import ListTableShell from '@/domain/common/page/list/ListTableShell';
import { operateLogApi } from '@/domain/sys/monitor/operateLog/api';
import type { OperateLogListVO } from '@/domain/sys/monitor/operateLog/types';

interface CurrentOperateLogModalProps {
  open: boolean;
  onClose: () => void;
}

const COLUMNS: ColumnsType<OperateLogListVO> = [
  { title: '发生时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', dataIndex: 'bizName', width: 180, render: (value) => value || '-' },
  {
    title: '结果',
    dataIndex: 'success',
    width: 80,
    render: (success: boolean) =>
      success ? <Tag color="success">成功</Tag> : <Tag color="error">失败</Tag>,
  },
  { title: '耗时(ms)', dataIndex: 'durationMs', width: 100 },
  { title: 'IP 地址', dataIndex: 'ip' },
];

export default function CurrentOperateLogModal({ open, onClose }: CurrentOperateLogModalProps) {
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const query = useQuery({
    queryKey: ['sys', 'monitor', 'operate-log', 'current', pageNum, pageSize],
    queryFn: () => operateLogApi.currentListPage({ pageNum, pageSize }),
    enabled: open,
    placeholderData: keepPreviousData,
  });
  const closeModal = () => {
    setPageNum(1);
    onClose();
  };

  return (
    <AppModal
      title="操作日志（近7天）"
      open={open}
      width={840}
      bodyMode="fixed"
      className="sm-current-audit-log-modal"
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
            className="sm-list-table"
            rowKey="id"
            size="small"
            loading={query.isLoading}
            columns={COLUMNS}
            dataSource={query.data?.records ?? []}
            pagination={false}
            sticky
            scroll={{ x: 720, y: 1 }}
          />
        }
      />
    </AppModal>
  );
}
