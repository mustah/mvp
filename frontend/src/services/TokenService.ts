const mvpToken = 'mvpToken';

export class TokenService {

  static makeToken(username: string, password: string): string {
    return btoa(`${username}:${password}`);
  }

  constructor(private storage: Storage) {
    this.storage = storage;
  }

  getToken(): string | null {
    return this.storage.getItem(mvpToken);
  }

  setToken(token: string | null) {
    if (token) {
      this.storage.setItem(mvpToken, token);
    }
  }

  clear(): void {
    this.storage.clear();
  }
}

export const tokenService = new TokenService(window.localStorage);
