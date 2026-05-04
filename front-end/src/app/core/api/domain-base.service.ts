import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  AuditLogModel,
  CategoryModel,
  OrderModel,
  PaymentModel,
  ProductModel
} from '../../shared/models/domain.models';

@Injectable({ providedIn: 'root' })
export class DomainBaseService {
  constructor(private readonly httpClient: HttpClient) {}

  listCategories(): Observable<CategoryModel[]> {
    return this.httpClient.get<CategoryModel[]>(`${this.apiBaseUrl}/categories`);
  }

  listProducts(): Observable<ProductModel[]> {
    return this.httpClient.get<ProductModel[]>(`${this.apiBaseUrl}/products`);
  }

  listOrders(): Observable<OrderModel[]> {
    return this.httpClient.get<OrderModel[]>(`${this.apiBaseUrl}/orders`);
  }

  listPayments(): Observable<PaymentModel[]> {
    return this.httpClient.get<PaymentModel[]>(`${this.apiBaseUrl}/payments`);
  }

  listAuditLogs(): Observable<AuditLogModel[]> {
    return this.httpClient.get<AuditLogModel[]>(`${this.apiBaseUrl}/audit-logs`);
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
