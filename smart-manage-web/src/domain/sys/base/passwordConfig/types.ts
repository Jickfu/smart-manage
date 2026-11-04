export interface PasswordConfigDetail {
  id: string;
  version: number;
  minLength: number;
  complexityEnabled: boolean;
}
export type PasswordConfigSaveForm = PasswordConfigDetail;
