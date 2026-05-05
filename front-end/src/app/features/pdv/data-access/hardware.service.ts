import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HardwareService {
  constructor(private readonly httpClient: HttpClient) {}

  openDrawer(): Observable<void> {
    return this.httpClient.post<void>('/api/hardware/drawer', {});
  }
}
