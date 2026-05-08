import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface SyncStatusCounts {
  [status: string]: number;
}

export interface SyncEvent {
  id: string;
  idempotencyKey: string;
  direction: 'LOCAL_TO_ONLINE' | 'ONLINE_TO_LOCAL';
  sourceEnvironment: 'LOCAL' | 'ONLINE';
  targetEnvironment: 'LOCAL' | 'ONLINE';
  aggregateType: string;
  aggregateId: string;
  eventType: string;
  payload: Record<string, unknown>;
  status: string;
  retryCount: number;
  lastError?: string;
  processedAt?: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class SyncService {
  private readonly apiUrl = `${environment.admin.apiBaseUrl}/sync`;

  constructor(private http: HttpClient) {}

  getSyncStatus(): Observable<SyncStatusCounts> {
    return this.http.get<SyncStatusCounts>(`${this.apiUrl}/status`);
  }

  getEvents(): Observable<SyncEvent[]> {
    // In a real app, this would have filters and pagination
    return this.http.get<SyncEvent[]>(`${this.apiUrl}/events`);
  }

  reprocessEvent(id: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/events/${id}/reprocess`, {});
  }

  ignoreEvent(id: string, reason: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/events/${id}/ignore`, { reason });
  }
}
