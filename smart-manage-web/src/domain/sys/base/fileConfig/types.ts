export interface FileConfigDetail {
  id?: string;
  version?: number;
  storageType: 'LOCAL' | 'FTP' | 'S3';
  localDir?: string;
  ftpHost?: string;
  ftpPort?: number;
  ftpUsername?: string;
  ftpPasswordConfigured: boolean;
  ftpDir?: string;
  ftpPassiveMode?: boolean;
  s3Endpoint?: string;
  s3Region?: string;
  s3Bucket?: string;
  s3AccessKey?: string;
  s3SecretKeyConfigured: boolean;
  s3PathStyle?: boolean;
}

export interface FileConfigSaveForm {
  id?: string;
  version?: number;
  storageType: 'LOCAL' | 'FTP' | 'S3';
  localDir?: string;
  ftpHost?: string;
  ftpPort?: number;
  ftpUsername?: string;
  ftpPassword?: string;
  ftpDir?: string;
  ftpPassiveMode?: boolean;
  s3Endpoint?: string;
  s3Region?: string;
  s3Bucket?: string;
  s3AccessKey?: string;
  s3SecretKey?: string;
  s3PathStyle?: boolean;
}
