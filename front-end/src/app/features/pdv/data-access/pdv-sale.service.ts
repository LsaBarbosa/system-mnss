import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { Uuid } from '../../../shared/models/domain.models';
import type {
  AddPdvSaleItemPayload,
  CancelSalePayload,
  CreateDiscountPayload,
  CreatePaymentPayload,
  PatchPdvSaleItemPayload,
  PaymentResponse,
  PdvSale
} from '../domain/pdv-sale.models';

@Injectable({ providedIn: 'root' })
export class PdvSaleService {
  constructor(private readonly httpClient: HttpClient) {}

  listSales(): Observable<PdvSale[]> {
    return this.httpClient.get<PdvSale[]>(`${this.apiBaseUrl}/pdv/sales`);
  }

  createSale(): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`${this.apiBaseUrl}/pdv/sales`, {});
  }

  getSale(saleId: Uuid): Observable<PdvSale> {
    return this.httpClient.get<PdvSale>(`${this.apiBaseUrl}/pdv/sales/${saleId}`);
  }

  addItem(saleId: Uuid, payload: AddPdvSaleItemPayload): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`${this.apiBaseUrl}/pdv/sales/${saleId}/items`, payload);
  }

  updateItem(saleId: Uuid, itemId: Uuid, payload: PatchPdvSaleItemPayload): Observable<PdvSale> {
    return this.httpClient.patch<PdvSale>(`${this.apiBaseUrl}/pdv/sales/${saleId}/items/${itemId}`, payload);
  }

  removeItem(saleId: Uuid, itemId: Uuid): Observable<PdvSale> {
    return this.httpClient.delete<PdvSale>(`${this.apiBaseUrl}/pdv/sales/${saleId}/items/${itemId}`);
  }

  pay(saleId: Uuid, payload: CreatePaymentPayload): Observable<PaymentResponse> {
    return this.httpClient.post<PaymentResponse>(`${this.apiBaseUrl}/pdv/sales/${saleId}/payment`, payload);
  }

  finish(saleId: Uuid): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`${this.apiBaseUrl}/pdv/sales/${saleId}/finish`, {});
  }

  applyDiscount(saleId: Uuid, payload: CreateDiscountPayload): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`${this.apiBaseUrl}/pdv/sales/${saleId}/discount`, payload);
  }

  cancelSale(saleId: Uuid, payload: CancelSalePayload): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`${this.apiBaseUrl}/pdv/sales/${saleId}/cancel`, payload);
  }

  reprintReceipt(saleId: Uuid): Observable<void> {
    return this.httpClient.post<void>(`${this.apiBaseUrl}/pdv/sales/${saleId}/print`, {});
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
