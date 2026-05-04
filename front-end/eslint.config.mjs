import js from '@eslint/js';
import tseslint from 'typescript-eslint';

const browserGlobals = {
  Event: 'readonly',
  HTMLElement: 'readonly',
  console: 'readonly',
  window: 'readonly'
};

const jasmineGlobals = {
  beforeEach: 'readonly',
  describe: 'readonly',
  expect: 'readonly',
  fail: 'readonly',
  it: 'readonly',
  jasmine: 'readonly'
};

export default tseslint.config(
  {
    ignores: ['dist/**', 'coverage/**', 'node_modules/**']
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ['src/**/*.ts'],
    languageOptions: {
      globals: {
        ...browserGlobals,
        ...jasmineGlobals
      }
    },
    rules: {
      '@typescript-eslint/no-explicit-any': 'error'
    }
  }
);
