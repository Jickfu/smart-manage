import request from '@/api/request';
import type { Result } from '@/types/api';
import type {
  InvocationStats,
  OneTimeCredential,
  OpenApiApplication,
  OpenApiApplicationForm,
  OpenApiCatalogHierarchyNode,
  OpenApiCredential,
  OpenApiInvocation,
  OpenApiListForm,
  OpenApiPageData,
  OpenApiRelease,
} from './types';

const post = <T>(url: string, data: unknown) =>
  request.post<Result<T>>(url, data).then((response) => response.data.data);

export const openApiPlatformApi = {
  applicationList: (form: OpenApiListForm) =>
    post<OpenApiPageData<OpenApiApplication>>('/sys/base/openapi/application/listPage', form),
  applicationDetail: (id: string) =>
    post<OpenApiApplication>('/sys/base/openapi/application/detail', { id }),
  applicationSave: (form: OpenApiApplicationForm) =>
    post<string>('/sys/base/openapi/application/save', form),
  applicationEnable: (form: { id: string; version: number; enabled: boolean }) =>
    post<void>('/sys/base/openapi/application/enable', form),
  credentialList: (applicationId: string) =>
    post<OpenApiCredential[]>('/sys/base/openapi/credential/list', { id: applicationId }),
  credentialCreate: (form: { applicationId: string; name: string; expiresAt?: string }) =>
    post<OneTimeCredential>('/sys/base/openapi/credential/create', form),
  credentialEnable: (form: { id: string; version: number; enabled: boolean }) =>
    post<void>('/sys/base/openapi/credential/enable', form),
  catalogList: (form: OpenApiListForm) =>
    post<OpenApiPageData<OpenApiRelease>>('/sys/base/openapi/catalog/listPage', form),
  catalogHierarchy: () =>
    request
      .get<Result<OpenApiCatalogHierarchyNode[]>>('/sys/base/openapi/catalog/hierarchy')
      .then((response) => response.data.data),
  catalogDetail: (id: string) => post<OpenApiRelease>('/sys/base/openapi/catalog/detail', { id }),
  catalogStatus: (form: { id: string; version: number; status: 'PUBLISHED' | 'OFFLINE' }) =>
    post<void>('/sys/base/openapi/catalog/status', form),
  invocationList: (form: OpenApiListForm) =>
    post<OpenApiPageData<OpenApiInvocation>>('/sys/base/openapi/invocation/listPage', form),
  invocationStats: () =>
    request
      .get<Result<InvocationStats>>('/sys/base/openapi/invocation/stats')
      .then((response) => response.data.data),
};
