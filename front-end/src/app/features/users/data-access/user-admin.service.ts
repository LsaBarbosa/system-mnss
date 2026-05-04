import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { AuthUser, CreateUserRequest, RoleOption } from '../../../core/auth/auth.models';

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  constructor(private readonly httpClient: HttpClient) {}

  listUsers(): Observable<AuthUser[]> {
    return this.httpClient.get<AuthUser[]>(`${this.apiBaseUrl}/users`);
  }

  listRoles(): Observable<RoleOption[]> {
    return this.httpClient.get<RoleOption[]>(`${this.apiBaseUrl}/roles`);
  }

  createUser(request: CreateUserRequest): Observable<AuthUser> {
    return this.httpClient.post<AuthUser>(`${this.apiBaseUrl}/users`, request);
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
