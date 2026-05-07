import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthTokenStorage {
  private readonly key = 'mnss.auth.token';

  getToken(): string | null {
    return window.sessionStorage.getItem(this.key);
  }

  setToken(token: string): void {
    window.sessionStorage.setItem(this.key, token);
  }

  clear(): void {
    window.sessionStorage.removeItem(this.key);
  }
}
