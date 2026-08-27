import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it } from 'vitest';
import { FormFieldCell, FormFieldGrid } from './FormFieldLayout';

describe('FormFieldLayout', () => {
  it('保持既有字段容器、跨列修饰类和直接子元素结构', () => {
    const markup = renderToStaticMarkup(
      <FormFieldGrid>
        <FormFieldCell>
          <div className="sm-edit-field-content">普通字段</div>
        </FormFieldCell>
        <FormFieldCell columnSpan={2}>
          <div className="sm-edit-field-content">占两列字段</div>
        </FormFieldCell>
        <FormFieldCell columnSpan={3}>
          <div className="sm-edit-field-content">占三列字段</div>
        </FormFieldCell>
        <FormFieldCell fullWidth>
          <div className="sm-edit-field-content">全宽字段</div>
        </FormFieldCell>
      </FormFieldGrid>,
    );

    expect(markup).toBe(
      '<div class="sm-edit-fields">' +
        '<div class="sm-edit-field"><div class="sm-edit-field-content">普通字段</div></div>' +
        '<div class="sm-edit-field sm-edit-field--span-2"><div class="sm-edit-field-content">占两列字段</div></div>' +
        '<div class="sm-edit-field sm-edit-field--span-3"><div class="sm-edit-field-content">占三列字段</div></div>' +
        '<div class="sm-edit-field sm-edit-field--full"><div class="sm-edit-field-content">全宽字段</div></div>' +
        '</div>',
    );
  });

  it('为两列和单列字段容器提供独立的整体居中变体', () => {
    expect(renderToStaticMarkup(<FormFieldGrid maxColumns={2}>两列</FormFieldGrid>)).toBe(
      '<div class="sm-edit-fields sm-edit-fields--max-2">两列</div>',
    );
    expect(renderToStaticMarkup(<FormFieldGrid maxColumns={1}>单列</FormFieldGrid>)).toBe(
      '<div class="sm-edit-fields sm-edit-fields--max-1">单列</div>',
    );
  });
});
