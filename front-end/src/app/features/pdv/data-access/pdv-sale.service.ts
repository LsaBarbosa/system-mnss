import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type {
  DecimalString,
  DeliveryType,
  IsoDateTime,
  OrderItemStatus,
  OrderOrigin,
  OrderStatus,
  PaymentMethod,
  PaymentStatus,
  PreparationSector,
  SyncStatus,
  Uuid
} from '../../../shared/models/domain.models';

export interface PdvSaleItem {
  id: Uuid;
  orderId: Uuid;
  productId: Uuid | null;
  productNameSnapshot: string;
  quantity: DecimalString;
  unitPrice: DecimalString;
  totalPrice: DecimalString;
  observation: string | null;
  status: OrderItemStatus;
  preparationSector: PreparationSector;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface PdvSalePayment {
  id: Uuid;
  method: PaymentMethod;
  amount: DecimalString;
}

export interface CreateDiscountPayload {
  amount: string;
}

export interface CancelSalePayload {
  reason: string;
}

export interface PdvSale {
  id: Uuid;
  orderNumber: number | null;
  origin: OrderOrigin;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  deliveryType: DeliveryType;
  subtotal: DecimalString;
  discountAmount: DecimalString;
  deliveryFee: DecimalString;
  totalAmount: DecimalString;
  items: PdvSaleItem[];
  payments: PdvSalePayment[];
  remainingAmount: DecimalString;
  syncStatus: SyncStatus | null;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface AddPdvSaleItemPayload {
  productId: Uuid;
  quantity?: DecimalString | null;
  observation?: string | null;
}

export interface PatchPdvSaleItemPayload {
  quantity: DecimalString;
}

export interface CreatePaymentPayload {
  method: PaymentMethod;
  amount: DecimalString;
  transactionId?: string | null;
  gateway?: string | null;
}

export interface PaymentResponse {
  id: Uuid;
  orderId: Uuid;
  method: PaymentMethod;
  status: PaymentStatus;
  recordedAmount: DecimalString;
  remainingAmount: DecimalString;
  changeAmount: DecimalString;
  transactionId: string | null;
  gateway: string | null;
  paidAt: IsoDateTime | null;
  canceledAt: IsoDateTime | null;
  orderStatus: OrderStatus;
  orderPaymentStatus: PaymentStatus;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

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
    return this.httpClient.post<PaymentResponse>(`/api/pdv/sales/${saleId}/payment`, payload);
  }

  finish(saleId: Uuid): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`/api/pdv/sales/${saleId}/finish`, {});
  }

  applyDiscount(saleId: Uuid, payload: CreateDiscountPayload): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`/api/pdv/sales/${saleId}/discount`, payload);
  }

  cancelSale(saleId: Uuid, payload: CancelSalePayload): Observable<PdvSale> {
    return this.httpClient.post<PdvSale>(`/api/pdv/sales/${saleId}/cancel`, payload);
  }

  reprintReceipt(saleId: Uuid): Observable<void> {
    return this.httpClient.post<void>(`/api/pdv/sales/${saleId}/print`, {});
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
