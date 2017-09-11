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
    this.storage.setItem(key, data);
  }

  removeItem(key: string): void {
    this.storage.removeItem(key);
  }

  clear(): void {
    this.storage.clear();
  }
}

export const storageService = new StorageService(window.localStorage);
