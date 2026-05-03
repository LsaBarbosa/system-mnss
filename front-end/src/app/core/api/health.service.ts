import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import type { HealthStatus } from './health-status';

@Injectable({ providedIn: 'root' })
export class HealthService {
  constructor(private readonly httpClient: HttpClient) {}

  getLocalHealth(): Observable<HealthStatus> {
    return this.httpClient.get<HealthStatus>('/api/health');
  }
}
