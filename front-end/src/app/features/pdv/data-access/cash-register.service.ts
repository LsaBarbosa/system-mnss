import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type {
  CashMovementModel,
  CashMovementType,
  CashRegisterModel,
  DecimalString,
  PaymentMethod,
  Uuid
} from '../../../shared/models/domain.models';

export interface CashRegisterOpenPayload {
  openingAmount: DecimalString;
  notes?: string | null;
}

export interface CashRegisterClosePayload {
  closingAmount: DecimalString;
  notes?: string | null;
}

export interface CashMovementPayload {
  type: CashMovementType;
  paymentMethod?: PaymentMethod | null;
  amount: DecimalString;
  description?: string | null;
  orderId?: Uuid | null;
}

export type CashMovementResponse = CashMovementModel;

export interface CurrentCashRegisterResponse {
  open: boolean;
  cashRegister: CashRegisterModel | null;
}

export interface CashRegisterSummaryResponse {
  cashRegister: CashRegisterModel;
  totalsByPaymentMethod: Record<PaymentMethod, DecimalString>;
  saleTotal: DecimalString;
  refundTotal: DecimalString;
  cashInTotal: DecimalString;
  cashOutTotal: DecimalString;
  adjustmentTotal: DecimalString;
  expectedAmount: DecimalString;
  closingAmount: DecimalString | null;
  differenceAmount: DecimalString;
  movements: CashMovementResponse[];
}

@Injectable({ providedIn: 'root' })
export class CashRegisterService {
  constructor(private readonly httpClient: HttpClient) {}

  open(payload: CashRegisterOpenPayload): Observable<CashRegisterModel> {
    return this.httpClient.post<CashRegisterModel>(`${this.apiBaseUrl}/cash-register/open`, payload);
  }

  current(): Observable<CurrentCashRegisterResponse> {
    return this.httpClient.get<CurrentCashRegisterResponse>(`${this.apiBaseUrl}/cash-register/current`);
  }

  createMovement(cashRegisterId: Uuid, payload: CashMovementPayload): Observable<CashMovementResponse> {
    return this.httpClient.post<CashMovementResponse>(
      `${this.apiBaseUrl}/cash-register/${cashRegisterId}/movement`,
      payload
    );
  }

  close(cashRegisterId: Uuid, payload: CashRegisterClosePayload): Observable<CashRegisterSummaryResponse> {
    return this.httpClient.post<CashRegisterSummaryResponse>(
      `${this.apiBaseUrl}/cash-register/${cashRegisterId}/close`,
      payload
    );
  }

  summary(cashRegisterId: Uuid): Observable<CashRegisterSummaryResponse> {
    return this.httpClient.get<CashRegisterSummaryResponse>(
      `${this.apiBaseUrl}/cash-register/${cashRegisterId}/summary`
    );
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
