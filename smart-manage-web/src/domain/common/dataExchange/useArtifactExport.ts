import { useCommandMutation } from '@/domain/common/page/useCommandMutation';
import type { FileArtifactReference } from '@/domain/common/fileArtifactApi';
import { downloadFileArtifact } from '@/domain/common/fileArtifactApi';

export const useArtifactExport = <TVariables>(
  exportCommand: (variables: TVariables) => Promise<FileArtifactReference>,
  successMessage: string,
) =>
  useCommandMutation({
    mutationFn: exportCommand,
    successMessage,
    onSuccess: downloadFileArtifact,
  });
