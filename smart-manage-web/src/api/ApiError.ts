export type ErrorSource =
  | 'API'
  | 'HTTP'
  | 'NETWORK'
  | 'TIMEOUT'
  | 'PROTOCOL'
  | 'CLIENT'
  | 'CANCELED';
export type FeedbackLevel = 'WARNING' | 'ERROR';

interface ApiErrorOptions {
  source: Exclude<ErrorSource, 'CANCELED'>;
  message: string;
  httpStatus?: number;
  apiCode?: number;
  feedbackLevel?: FeedbackLevel;
  traceId?: string | null;
  data?: unknown;
}

/** 请求失败的规范模型。HTTP 状态与业务码互不替代，取消仍保留原始取消对象。 */
export class ApiError extends Error {
  readonly source: ApiErrorOptions['source'];
  readonly httpStatus?: number;
  readonly apiCode?: number;
  readonly feedbackLevel: FeedbackLevel;
  readonly traceId: string;
  readonly data?: unknown;

  constructor(options: ApiErrorOptions) {
    super(options.message);
    this.name = 'ApiError';
    this.source = options.source;
    this.httpStatus = options.httpStatus;
    this.apiCode = options.apiCode;
    this.feedbackLevel = options.feedbackLevel ?? 'ERROR';
    this.traceId = options.traceId ?? '';
    this.data = options.data;
  }
}
