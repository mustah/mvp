export class StorageService {

  constructor(private storage: Storage) {
    this.storage = storage;
  }

  getItem(key: string): string | null {
    try {
      return this.storage.getItem(key);
    } catch (error) {
      return null;
    }
  }

  setItem(key: string, data: string): void {
    try {
      this.storage.setItem(key, data);
    } catch (error) {
      // ignore write errors - just don't fail the app
    }
  }

  removeItem(key: string): void {
    try {
      this.storage.removeItem(key);
    } catch (error) {
      // ignore remove errors - just don't fail the app
    }
  }

  clear(): void {
    this.storage.clear();
  }
}

export const storageService = new StorageService(window.localStorage);
