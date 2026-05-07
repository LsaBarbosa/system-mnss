import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PublicMenuResponse, StoreInfoResponse } from '../domain/public-menu.model';

@Injectable({
  providedIn: 'root'
})
export class PublicMenuService {
  constructor(private readonly http: HttpClient) {}

  private get baseUrl(): string {
    const runtime: string = environment.runtime;
    const apiBaseUrl = runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
    return `${apiBaseUrl}/public/menu`;
  }

  getMenu(search?: string): Observable<PublicMenuResponse[]> {
    let params = new HttpParams();
    if (search) {
      params = params.set('search', search);
    }
    return this.http.get<PublicMenuResponse[]>(this.baseUrl, { params });
  }

  getStoreInfo(): Observable<StoreInfoResponse> {
    return this.http.get<StoreInfoResponse>(`${this.baseUrl}/info`);
  }
}
