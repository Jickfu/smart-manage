import { Button } from 'antd';
import { useQuery } from '@tanstack/react-query';
import AppModal from '@/domain/common/component/AppModal';
import { frontendVersion, getBackendVersion } from '@/api/productVersion';

export default function AboutProductModal({
  systemName,
  logo,
  onClose,
}: {
  systemName: string;
  logo: string;
  onClose: () => void;
}) {
  // 每次打开重新查询实际后端；失败时不把旧缓存冒充当前服务的版本。
  const backendQuery = useQuery({
    queryKey: ['sys', 'product', 'version'],
    queryFn: getBackendVersion,
    staleTime: 0,
    retry: false,
    refetchOnWindowFocus: false,
  });

  return (
    <AppModal
      title="关于产品"
      open
      width={440}
      bodyMode="natural"
      onCancel={onClose}
      footer={<Button onClick={onClose}>关闭</Button>}
    >
      <div className="sm-about-product">
        <img src={logo} alt={systemName} />
        <strong>{systemName}</strong>
        <dl className="sm-about-versions">
          <div>
            <dt>前端版本</dt>
            <dd>{frontendVersion}</dd>
          </div>
          <div>
            <dt>后端版本</dt>
            <dd>
              {backendQuery.isFetching
                ? '加载中…'
                : backendQuery.isError
                  ? '暂不可用'
                  : backendQuery.data
                    ? backendQuery.data.version || '版本不可用'
                    : '暂不可用'}
            </dd>
          </div>
        </dl>
      </div>
    </AppModal>
  );
}
