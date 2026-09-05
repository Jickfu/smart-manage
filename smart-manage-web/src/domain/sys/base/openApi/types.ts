import type { PageData, PageForm } from '@/types/api';

export type EncryptionAlgorithm = 'NONE' | 'AES_256_GCM' | 'SM4_GCM';
export type IpPolicyMode = 'DISABLED' | 'WHITELIST' | 'BLACKLIST';

export interface ReferenceRecord {
  id: string;
  number: string;
  name: string;
  username?: string;
  namePath?: string;
}

export interface OpenApiCredential {
  id: string;
  applicationId: string;
  keyId: string;
  name: string;
  enabled: boolean;
  encryptionAlgorithm: EncryptionAlgorithm;
  expiresAt?: string;
  lastUsedAt?: string;
  createTime: string;
  version: number;
}

export interface OneTimeCredential extends OpenApiCredential {
  signingSecret: string;
  requestEncryptionKey?: string;
  responseEncryptionKey?: string;
  oneTimeVisible: true;
}

export interface OpenApiApplication {
  id: string;
  number: string;
  name: string;
  enabled: boolean;
  proxyUserId: string;
  proxyOrgId: string;
  proxyUser?: ReferenceRecord;
  proxyOrg?: ReferenceRecord;
  authenticationType: 'HMAC_SHA256';
  encryptionAlgorithm: EncryptionAlgorithm;
  ipPolicyMode: IpPolicyMode;
  ipRanges?: string;
  description?: string;
  operationKeys?: string[];
  credentials?: OpenApiCredential[];
  version: number;
  createTime: string;
  updateTime?: string;
}

export interface OpenApiApplicationForm {
  id?: string;
  version?: number;
  number: string;
  name: string;
  proxyUserId: string;
  proxyOrgId: string;
  authenticationType: 'HMAC_SHA256';
  encryptionAlgorithm: EncryptionAlgorithm;
  ipPolicyMode: IpPolicyMode;
  ipRanges?: string;
  description?: string;
  operationKeys: string[];
}

export interface OpenApiRelease {
  id: string;
  apiNumber: string;
  apiVersion: string;
  operationKey: string;
  name: string;
  httpMethod: string;
  path: string;
  domainKey: string;
  domainName: string;
  applicationKey: string;
  applicationName: string;
  featureKey: string;
  featureName: string;
  status: 'DRAFT' | 'PUBLISHED' | 'OFFLINE';
  description?: string;
  requestSchema: string;
  responseSchema: string;
  documentation?: string;
  requestExample?: string;
  responseExample?: string;
  systemPreset: boolean;
  registered: boolean;
  testable: boolean;
  version: number;
}

export interface OpenApiTestApplication {
  id: string;
  number: string;
  name: string;
}

export interface OpenApiTestResult {
  requestId: string;
  durationMs: number;
  response: unknown;
}

export interface OpenApiInvocation {
  id: string;
  requestTime: string;
  applicationId?: string;
  applicationNumber?: string;
  credentialKeyId?: string;
  operationKey?: string;
  requestId?: string;
  traceId?: string;
  clientIp?: string;
  resultType: string;
  resultCode: number;
  durationMs: number;
  requestBytes: number;
  responseBytes: number;
  errorMessage?: string;
}

export interface OpenApiListForm extends PageForm {
  keyword?: string;
  enabled?: boolean;
  applicationId?: string;
  operationKey?: string;
  resultType?: string;
  domainKey?: string;
  applicationKey?: string;
  featureKey?: string;
}

export interface OpenApiCatalogHierarchyNode {
  key: string;
  parentKey?: string;
  type: 'DOMAIN' | 'APPLICATION' | 'FEATURE';
  title: string;
  children?: OpenApiCatalogHierarchyNode[];
}

export interface InvocationStats {
  summary: Record<string, number>;
  operations: Array<Record<string, string | number>>;
}

export type OpenApiPageData<T> = PageData<T>;
