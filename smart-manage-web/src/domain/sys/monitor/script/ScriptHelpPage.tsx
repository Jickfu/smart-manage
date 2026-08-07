import { useMemo, useState } from 'react';
import {
  App,
  Alert,
  Button,
  Collapse,
  Input,
  Segmented,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { CopyOutlined, ReloadOutlined } from '@ant-design/icons';
import { useQuery } from '@tanstack/react-query';
import { EditPageShell } from '@/domain/common/page/EditPageShell';
import type { PageComponentProps } from '@/domain/common/page/types';
import { scriptApi } from './api';
import { scriptQueryKeys } from './queryKeys';
import { scriptTemplates } from './scriptTemplates';
import type { ScriptApiField, ScriptApiMethod, ScriptApiService } from './types';
import './scriptHelp.css';

type HelpSection = 'guide' | 'templates' | 'api';

const fieldColumns: ColumnsType<ScriptApiField> = [
  { title: '字段', dataIndex: 'name', width: 180 },
  { title: '类型', dataIndex: 'type', width: 220 },
  {
    title: '必填',
    dataIndex: 'required',
    width: 80,
    render: (required: boolean) => (required ? <Tag color="error">是</Tag> : '否'),
  },
  {
    title: '约束',
    dataIndex: 'constraints',
    render: (constraints: string[]) => constraints.join('；') || '-',
  },
];

export default function ScriptHelpPage(_props: PageComponentProps) {
  const { message } = App.useApp();
  const [section, setSection] = useState<HelpSection>('guide');
  const [keyword, setKeyword] = useState('');
  const apiQuery = useQuery({
    queryKey: scriptQueryKeys.apiMetadata(),
    queryFn: scriptApi.apiMetadata,
  });
  const services = useMemo(() => {
    const normalized = keyword.trim().toLowerCase();
    if (!normalized) return apiQuery.data ?? [];
    return (apiQuery.data ?? [])
      .map((service) => ({
        ...service,
        methods: service.methods.filter(
          (method) =>
            service.beanName.toLowerCase().includes(normalized) ||
            service.className.toLowerCase().includes(normalized) ||
            method.name.toLowerCase().includes(normalized) ||
            method.signature.toLowerCase().includes(normalized),
        ),
      }))
      .filter((service) => service.methods.length > 0);
  }, [apiQuery.data, keyword]);

  const copy = async (content: string) => {
    await navigator.clipboard.writeText(content);
    message.success('示例已复制');
  };

  return (
    <EditPageShell
      title="脚本使用帮助"
      loading={apiQuery.isLoading}
      error={apiQuery.error}
      onRetry={() => apiQuery.refetch()}
      actions={
        <Button icon={<ReloadOutlined />} onClick={() => apiQuery.refetch()}>
          刷新 API
        </Button>
      }
    >
      <div className="sm-script-help">
        <div className="sm-script-help-heading">
          <div>
            <Typography.Title level={4}>脚本控制台使用帮助</Typography.Title>
            <Typography.Text type="secondary">
              从模板开始，或者查询当前服务实际开放的领域 Service 和方法。
            </Typography.Text>
          </div>
          <Segmented<HelpSection>
            value={section}
            options={[
              { value: 'guide', label: '快速开始' },
              { value: 'templates', label: '脚本模板' },
              { value: 'api', label: `Service API（${apiQuery.data?.length ?? 0}）` },
            ]}
            onChange={setSection}
          />
        </div>

        {section === 'guide' && <QuickGuide />}
        {section === 'templates' && (
          <Collapse
            className="sm-script-help-collapse"
            items={scriptTemplates.map((template) => ({
              key: template.key,
              label: template.name,
              children: (
                <div className="sm-script-help-example">
                  <Typography.Paragraph>{template.description}</Typography.Paragraph>
                  <Button
                    size="small"
                    icon={<CopyOutlined />}
                    onClick={() => copy(template.content)}
                  >
                    复制模板
                  </Button>
                  <pre>{template.content}</pre>
                </div>
              ),
            }))}
          />
        )}
        {section === 'api' && (
          <ApiReference
            services={services}
            keyword={keyword}
            onKeywordChange={setKeyword}
            onCopy={copy}
          />
        )}
      </div>
    </EditPageShell>
  );
}

function QuickGuide() {
  return (
    <div className="sm-script-help-guide">
      <Alert
        type="warning"
        showIcon
        title="脚本以管理员权限在服务端执行"
        description="默认选择原子事务。普通 REQUIRED 数据库操作可在失败时整体回滚，但独立事务、异步任务、远程调用、消息和文件操作无法随外层事务回滚。"
      />
      <section>
        <Typography.Title level={5}>1. 获取领域 Service</Typography.Title>
        <pre>{`const service = app.getService('userService');`}</pre>
        <Typography.Paragraph>
          Bean 必须是 <Typography.Text code>sm.domain.*</Typography.Text> 下公开且类名以{' '}
          <Typography.Text code>Service</Typography.Text> 结尾的领域 Service。
        </Typography.Paragraph>
      </section>
      <section>
        <Typography.Title level={5}>2. 传递参数并调用方法</Typography.Title>
        <pre>{`const result = service.detail(10001);

const saved = service.save({
  id: 10001,
  version: 0,
});`}</pre>
        <Typography.Paragraph>
          JavaScript 对象会根据 Java 方法参数声明转换为对应 Form；参数字段可在“Service API”中查询。
        </Typography.Paragraph>
      </section>
      <section>
        <Typography.Title level={5}>3. 查看结果</Typography.Title>
        <pre>{`console.log(result);
return result;`}</pre>
        <Typography.Paragraph>
          <Typography.Text code>console.log</Typography.Text> 和脚本返回值都会显示在结果区。按{' '}
          <Typography.Text keyboard>Ctrl + E</Typography.Text> 执行选区；没有选区时执行全文。
        </Typography.Paragraph>
      </section>
      <section>
        <Typography.Title level={5}>安全限制</Typography.Title>
        <Typography.Paragraph>
          脚本不能访问 Java 类、反射、文件、网络、进程、线程、Mapper、TxService、数据源或任意 Spring
          Bean。脚本还受系统配置的源码长度、输出长度和执行超时限制。
        </Typography.Paragraph>
      </section>
    </div>
  );
}

interface ApiReferenceProps {
  services: ScriptApiService[];
  keyword: string;
  onKeywordChange: (value: string) => void;
  onCopy: (content: string) => void;
}

function ApiReference({ services, keyword, onKeywordChange, onCopy }: ApiReferenceProps) {
  return (
    <div className="sm-script-api-reference">
      <Input
        allowClear
        value={keyword}
        placeholder="搜索 Bean、类名或方法名"
        onChange={(event) => onKeywordChange(event.target.value)}
      />
      <Collapse
        className="sm-script-help-collapse"
        items={services.map((service) => ({
          key: service.beanName,
          label: (
            <Space wrap>
              <Typography.Text strong>{service.beanName}</Typography.Text>
              <Typography.Text type="secondary">{service.className}</Typography.Text>
              <Tag>{service.methods.length} 个方法</Tag>
            </Space>
          ),
          children: <ServiceMethods service={service} onCopy={onCopy} />,
        }))}
      />
      {!services.length && (
        <Typography.Text type="secondary">没有匹配的 Service API。</Typography.Text>
      )}
    </div>
  );
}

function ServiceMethods({
  service,
  onCopy,
}: {
  service: ScriptApiService;
  onCopy: (value: string) => void;
}) {
  return (
    <Collapse
      ghost
      items={service.methods.map((method, index) => ({
        key: `${method.signature}-${index}`,
        label: <Typography.Text code>{method.signature}</Typography.Text>,
        children: <MethodDetail method={method} onCopy={onCopy} />,
      }))}
    />
  );
}

function MethodDetail({
  method,
  onCopy,
}: {
  method: ScriptApiMethod;
  onCopy: (value: string) => void;
}) {
  return (
    <div className="sm-script-api-method">
      {method.parameters.map((parameter) => (
        <section key={parameter.name}>
          <Typography.Text strong>
            参数：{parameter.name}（{parameter.type}）
          </Typography.Text>
          {parameter.fields.length > 0 && (
            <Table<ScriptApiField>
              size="small"
              rowKey="name"
              pagination={false}
              columns={fieldColumns}
              dataSource={parameter.fields}
              scroll={{ x: 700 }}
            />
          )}
        </section>
      ))}
      <div className="sm-script-help-example">
        <Button size="small" icon={<CopyOutlined />} onClick={() => onCopy(method.example)}>
          复制调用示例
        </Button>
        <pre>{method.example}</pre>
      </div>
    </div>
  );
}
