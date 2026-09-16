/// <reference types="vite/client" />

declare const __PRODUCT_VERSION__: string;

interface ImportMetaEnv {
  readonly VITE_API_BASE_PATH: string;
}
