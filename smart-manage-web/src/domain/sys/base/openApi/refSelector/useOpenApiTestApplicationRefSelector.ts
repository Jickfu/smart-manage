import { useMemo } from 'react';
import type { RefSelectorFieldConfig } from '@/domain/common/page/edit/EditPage';
import { defineRefSelector } from '@/domain/common/page/edit/defineRefSelector';
import type { OpenApiTestApplication } from '../types';

/** API 试调应用引用选择器；候选项仅来自后端返回的当前接口已授权应用。 */
export function useOpenApiTestApplicationRefSelector(
  releaseId: string | undefined,
  applications: OpenApiTestApplication[],
): RefSelectorFieldConfig {
  return useMemo(
    () =>
      defineRefSelector<OpenApiTestApplication>({
        selectorKey: ['sys-base-openapi-test-application', releaseId],
        modalTitle: '选择第三方应用',
        fetchFn: async ({ pageNum, pageSize, keyword }) => {
          const normalizedKeyword = keyword?.trim().toLowerCase();
          const filtered = normalizedKeyword
            ? applications.filter(
                (application) =>
                  application.number.toLowerCase().includes(normalizedKeyword) ||
                  application.name.toLowerCase().includes(normalizedKeyword),
              )
            : applications;
          const offset = (pageNum - 1) * pageSize;
          return {
            records: filtered.slice(offset, offset + pageSize),
            total: filtered.length,
          };
        },
        displayRender: (application) => `${application.number} - ${application.name}`,
        fieldNames: { key: 'id', label: 'name' },
        columns: [
          { title: '应用编码', dataIndex: 'number', width: 200 },
          { title: '应用名称', dataIndex: 'name' },
        ],
      }),
    [applications, releaseId],
  );
}
