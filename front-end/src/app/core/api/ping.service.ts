import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { catchError, map, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { PingResponse, PingStatus } from './ping-status';

@Injectable({ providedIn: 'root' })
export class PingService {
  constructor(private readonly httpClient: HttpClient) {}

  checkLocalApi(): Observable<PingStatus> {
    return this.httpClient.get<PingResponse>(`${environment.localApiBaseUrl}/ping`).pipe(
      map((response) => ({ available: true, response }) as const),
      catchError(() =>
        of({
          available: false,
          errorMessage: 'API local indisponivel'
        } as const)
      )
    );
  }
}
