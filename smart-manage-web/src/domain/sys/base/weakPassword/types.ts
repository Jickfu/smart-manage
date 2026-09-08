import type { PageForm } from '@/types/api';

export interface WeakPasswordListForm extends PageForm {
  keyword?: string;
}

export interface WeakPasswordVO {
  id: string;
  version: number;
  word: string;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

export interface WeakPasswordSaveForm {
  id?: string;
  version?: number;
  word: string;
  description?: string;
}
