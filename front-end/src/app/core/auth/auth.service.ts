import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, type Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { AuthResponse, AuthUser, LoginRequest, RoleName } from './auth.models';
import { AuthTokenStorage } from './auth-token.storage';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly userSubject = new BehaviorSubject<AuthUser | null>(null);
  readonly currentUser$ = this.userSubject.asObservable();

  constructor(
    private readonly httpClient: HttpClient,
    private readonly tokenStorage: AuthTokenStorage
  ) {}

  get currentUser(): AuthUser | null {
    return this.userSubject.value;
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.httpClient.post<AuthResponse>(`${this.apiBaseUrl}/auth/login`, request).pipe(
      tap((response) => {
        this.tokenStorage.setToken(response.token);
        this.userSubject.next(response.user);
      })
    );
  }

  loadMe(): Observable<AuthUser> {
    return this.httpClient.get<AuthUser>(`${this.apiBaseUrl}/auth/me`).pipe(
      tap((user) => {
        this.userSubject.next(user);
      })
    );
  }

  logout(): void {
    this.tokenStorage.clear();
    this.userSubject.next(null);
  }

  isAuthenticated(): boolean {
    return this.tokenStorage.getToken() !== null;
  }

  hasAnyRole(requiredRoles: readonly RoleName[]): boolean {
    const user = this.currentUser;
    if (!user) {
      return false;
    }

    return user.roles.includes('ADMIN') || requiredRoles.some((role) => user.roles.includes(role));
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
