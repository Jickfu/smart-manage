export interface EmailAccount {
  id: string;
  number: string;
  name: string;
  host: string;
  port: number;
  securityMode: 'NONE' | 'STARTTLS' | 'SSL_TLS';
  username: string;
  passwordConfigured: boolean;
  fromAddress: string;
  fromName?: string;
  replyTo?: string;
  enabled: boolean;
  defaultAccount: boolean;
  allowManual: boolean;
  connectionTimeoutMs: number;
  readTimeoutMs: number;
  description?: string;
  version: number;
}
export interface AccountForm extends Omit<EmailAccount, 'id' | 'passwordConfigured' | 'enabled'> {
  id?: string;
  password?: string;
}
export interface AccountOption {
  id: string;
  number: string;
  name: string;
  defaultAccount: boolean;
  fromAddress: string;
}
export interface EmailRecord {
  id: string;
  sourceTaskId?: string;
  accountId: string;
  accountNumber: string;
  fromAddress: string;
  to: string[];
  cc?: string[];
  bcc?: string[];
  subject: string;
  htmlBody?: string;
  textBody?: string;
  status: string;
  attemptCount: number;
  maxAttempts: number;
  errorCategory?: string;
  errorMessage?: string;
  createTime: string;
  completedTime?: string;
  version: number;
  attempts?: Array<Record<string, unknown>>;
}
export interface PageData<T> {
  pageNum: number;
  pageSize: number;
  total: number;
  records: T[];
}
