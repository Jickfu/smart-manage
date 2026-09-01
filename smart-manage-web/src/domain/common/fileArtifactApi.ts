import request from '@/api/request';

export interface FileArtifactReference {
  id: string;
  originalName: string;
  expiresAt: string;
}

export async function downloadFileArtifact(artifact: FileArtifactReference) {
  const response = await request.post<Blob>(
    '/sys/base/file-artifact/download',
    { id: artifact.id },
    { responseType: 'blob' },
  );
  const url = URL.createObjectURL(response.data);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = artifact.originalName;
  anchor.click();
  window.setTimeout(() => URL.revokeObjectURL(url), 0);
}
