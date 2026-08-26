import { describe, expect, it } from 'vitest';
import { buildAttachmentFormValues } from './useEditAttachments';

describe('buildAttachmentFormValues', () => {
  it('保存时只为临时附件组装上传会话', () => {
    expect(
      buildAttachmentFormValues({ name: '单据' }, [
        { id: 'stored', businessAttachmentId: 'stored', originalName: '已保存' },
        {
          id: 'temp',
          businessAttachmentId: 'temp',
          originalName: '待提交',
          isTemp: true,
          uploadSessionId: 'session',
        },
      ]),
    ).toEqual({
      name: '单据',
      attachmentIds: ['stored', 'temp'],
      attachmentUploadSessions: { temp: 'session' },
    });
  });
});
