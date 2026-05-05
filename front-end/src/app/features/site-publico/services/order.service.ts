import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface CustomerRequest {
  name: string;
  phone: string;
  email?: string;
  document?: string;
}

export interface AddressRequest {
  street: string;
  number?: string;
  neighborhood?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  complement?: string;
  reference?: string;
}

export interface OrderItemRequest {
  productId: string;
  quantity: number;
  observation?: string;
}

export interface CreateOrderRequest {
  customer: CustomerRequest;
  deliveryType: 'PICKUP' | 'DELIVERY';
  address?: AddressRequest;
  items: OrderItemRequest[];
  notes?: string;
}

export interface OrderResponse {
  id: string;
  orderNumber: number;
  status: string;
  paymentStatus: string;
  deliveryType: string;
  subtotal: number;
  discountAmount: number;
  deliveryFee: number;
  totalAmount: number;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }

  private get apiUrl(): string {
    return `${this.apiBaseUrl}/public/orders`;
  }

  constructor(private http: HttpClient) {}

  createOrder(request: CreateOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(this.apiUrl, request);
  }
}
