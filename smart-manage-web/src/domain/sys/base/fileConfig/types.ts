export interface FileConfigDetail {
  id?: string;
  version?: number;
  storageType: 'LOCAL' | 'FTP';
  localDir?: string;
  ftpHost?: string;
  ftpPort?: number;
  ftpUsername?: string;
  ftpPasswordConfigured: boolean;
  ftpDir?: string;
  ftpPassiveMode?: boolean;
}

export interface FileConfigSaveForm {
  id?: string;
  version?: number;
  storageType: 'LOCAL' | 'FTP';
  localDir?: string;
  ftpHost?: string;
  ftpPort?: number;
  ftpUsername?: string;
  ftpPassword?: string;
  ftpDir?: string;
  ftpPassiveMode?: boolean;
}
