import type { PageForm } from '@/types/api';

export interface BasicDataEntry {
  id?: string;
  number: string;
  name: string;
  sort: number;
  enabled: boolean;
}

export interface BasicDataListForm extends PageForm {
  keyword?: string;
}

export interface BasicDataListVO {
  id: string;
  version: number;
  number: string;
  name: string;
  remark?: string;
  enabled: boolean;
  createTime?: string;
  updateTime?: string;
}

export interface BasicDataDetailVO extends BasicDataListVO {
  entrys: BasicDataEntry[];
}

export interface BasicDataCreateNewDataVO {
  enabled: boolean;
  entrys: BasicDataEntry[];
}

export interface BasicDataSaveForm {
  id?: string;
  version?: number;
  number: string;
  name: string;
  remark?: string;
  entrys: BasicDataEntry[];
}
