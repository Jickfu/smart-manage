import tseslint from 'typescript-eslint';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import prettier from 'eslint-config-prettier';
import globals from 'globals';

export default tseslint.config(
  // 忽略目录
  {
    ignores: ['dist', 'node_modules', 'public/js/*.js'],
  },
  // TypeScript 推荐规则
  ...tseslint.configs.recommended,
  // 源码文件自定义规则
  {
    files: ['src/**/*.{ts,tsx}'],
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    languageOptions: {
      globals: globals.browser,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
      'no-restricted-imports': [
        'error',
        {
          paths: [
            {
              name: '@ant-design/icons',
              message: '请从 @ant-design/icons/具体图标 独立导入，避免开发环境加载整套图标。',
            },
          ],
        },
      ],
    },
  },
  // Prettier 关掉所有与格式化冲突的 ESLint 规则
  prettier,
);
