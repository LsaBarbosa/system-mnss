import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { DecimalString, StockMovementModel, StockMovementType, Uuid } from '../../../shared/models/domain.models';

export interface StockMovementPayload {
  productId: Uuid;
  type: StockMovementType;
  quantity: DecimalString;
  reason?: string | null;
  orderId?: Uuid | null;
}

export interface StockMovementResponse extends StockMovementModel {
  productName: string;
}

export interface StockBalanceModel {
  productId: Uuid;
  productName: string;
  quantity: DecimalString;
}

@Injectable({ providedIn: 'root' })
export class StockService {
  constructor(private readonly httpClient: HttpClient) {}

  listMovements(productId?: Uuid | null): Observable<StockMovementResponse[]> {
    const params = productId ? new HttpParams().set('productId', productId) : undefined;
    return this.httpClient.get<StockMovementResponse[]>(`${this.apiBaseUrl}/stock-movements`, { params });
  }

  createMovement(payload: StockMovementPayload): Observable<StockMovementResponse> {
    return this.httpClient.post<StockMovementResponse>(`${this.apiBaseUrl}/stock-movements`, payload);
  }

  listBalances(): Observable<StockBalanceModel[]> {
    return this.httpClient.get<StockBalanceModel[]>(`${this.apiBaseUrl}/stock-movements/balances`);
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
