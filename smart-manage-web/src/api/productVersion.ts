import request from '@/api/request';
import type { Result } from '@/types/api';

export interface ProductVersion {
  version: string | null;
}

export const frontendVersion = __PRODUCT_VERSION__;

export const getBackendVersion = () =>
  request
    .get<Result<ProductVersion>>('/sys/base/product/version')
    .then((response) => response.data.data);
