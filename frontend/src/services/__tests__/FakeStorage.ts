export class FakeStorage implements Storage {

  [index: number]: string;
  [key: string]: any;
  length: number;

  private storage: {[key: string]: string} = {};

  getItem(key: string): string | null {
    return this.storage[key];
  }

  setItem(key: string, data: string): void {
    this.storage[key] = data;
  }

  clear(): void {
    this.storage = {};
  }

  key(index: number): string | null {
    throw new Error('Method not implemented.');
  }

  removeItem(key: string): void {
    delete this.storage[key];
  }

}
