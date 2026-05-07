import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface PaymentRequest {
  orderId: string;
  method: string;
}

export interface PaymentResponse {
  id: string;
  orderId: string;
  method: string;
  status: string;
  amount: number;
  transactionId: string;
  qrCodeBase64?: string;
  qrCodeCopyPaste?: string;
  paymentUrl?: string;
}

@Injectable({
  providedIn: 'root'
})
export class OnlinePaymentService {
  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }

  private get apiUrl(): string {
    return `${this.apiBaseUrl}/public/payments`;
  }

  constructor(private http: HttpClient) {}

  createPayment(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.apiUrl}/online`, request);
  }
}
