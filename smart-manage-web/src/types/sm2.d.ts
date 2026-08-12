interface Sm2BrowserApi {
  doEncrypt(data: string, publicKey: string, cipherMode: number): string;
}

interface Window {
  sm2: Sm2BrowserApi;
}
