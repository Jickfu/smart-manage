import type { FileArtifactReference } from '@/domain/common/fileArtifactApi';
import { downloadFileArtifact } from '@/domain/common/fileArtifactApi';

export const downloadBlob = (blob: Blob, fileName: string) => {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = fileName;
  anchor.click();
  window.setTimeout(() => URL.revokeObjectURL(url), 0);
};

export const downloadArtifacts = async (
  artifacts: Array<FileArtifactReference | null | undefined>,
) => {
  for (const artifact of artifacts) {
    if (artifact) await downloadFileArtifact(artifact);
  }
};
