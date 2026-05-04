import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthTokenStorage {
  private readonly key = 'mnss.auth.token';

  getToken(): string | null {
    return window.localStorage.getItem(this.key);
  }

  setToken(token: string): void {
    window.localStorage.setItem(this.key, token);
  }

  clear(): void {
    window.localStorage.removeItem(this.key);
  }
}
