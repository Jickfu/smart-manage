import { Card, Empty, Result, Statistic, Table } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { BillStatus } from '@/domain/common/page/types';
import QuickLaunchCard from '@/domain/common/home/QuickLaunchCard';
import HomeCardGrid from '@/domain/common/home/HomeCardGrid';
import { purchaseRequisitionApi } from '../purchaseRequisition/api';
import type { PurchaseRequisitionListVO } from '../purchaseRequisition/types';
import './ProcurementHome.css';

const statusLabels: Record<string, string> = {
  [BillStatus.SAVED]: '暂存',
  [BillStatus.SUBMITTED]: '已提交',
  [BillStatus.AUDITED]: '审核通过',
  [BillStatus.CLOSED]: '已关闭',
};

const ProcurementHome = () => {
  const summaryQuery = useQuery({
    queryKey: ['scm', 'procurement', 'home', 'summary'],
    queryFn: purchaseRequisitionApi.homeSummary,
  });
  if (summaryQuery.error) {
    return (
      <div className="sm-app-home">
        <QuickLaunchCard scope="APPLICATION" appNumber="procurement" />
        <Card className="sm-app-home-card">
          <Result status="error" title="采购概览加载失败" subTitle={summaryQuery.error.message} />
        </Card>
      </div>
    );
  }
  const summary = summaryQuery.data;
  return (
    <div className="sm-app-home">
      <QuickLaunchCard scope="APPLICATION" appNumber="procurement" />
      <HomeCardGrid className="sm-procurement-home-statistics">
        {Object.entries(statusLabels).map(([status, label]) => (
          <Card
            key={status}
            className="sm-app-home-card sm-procurement-home-statistic-card"
            loading={summaryQuery.isLoading}
          >
            <Statistic title={label} value={summary?.statusCounts[status] ?? 0} />
          </Card>
        ))}
      </HomeCardGrid>
      <Card className="sm-app-home-card" title="最近采购申请" loading={summaryQuery.isLoading}>
        {summary?.recent.length ? (
          <Table<PurchaseRequisitionListVO>
            rowKey="id"
            pagination={false}
            size="small"
            dataSource={summary.recent}
            columns={[
              { title: '编号', dataIndex: 'number', width: 190 },
              { title: '主题', dataIndex: 'subject' },
              {
                title: '状态',
                dataIndex: 'billStatus',
                width: 100,
                render: (value: string) => statusLabels[value] ?? value,
              },
              { title: '业务日期', dataIndex: 'bizDate', width: 120 },
            ]}
          />
        ) : (
          <Empty description="当前数据范围内暂无采购申请" />
        )}
      </Card>
    </div>
  );
};

export default ProcurementHome;
