import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { HealthStatus } from './health-status';

@Injectable({ providedIn: 'root' })
export class HealthService {
  constructor(private readonly httpClient: HttpClient) {}

  getLocalHealth(): Observable<HealthStatus> {
    const runtime: string = environment.runtime;
    const apiBaseUrl = runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;

    return this.httpClient.get<HealthStatus>(`${apiBaseUrl}/health`);
  }
}
