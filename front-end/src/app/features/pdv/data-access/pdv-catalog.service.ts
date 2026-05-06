import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { ProductModel } from '../../../shared/models/domain.models';
import type { PdvProductFilters, PdvProductGroup } from '../domain/pdv-catalog.models';

@Injectable({ providedIn: 'root' })
export class PdvCatalogService {
  constructor(private readonly httpClient: HttpClient) {}

  listProducts(filters: PdvProductFilters = {}): Observable<PdvProductGroup[]> {
    let params = new HttpParams();
    if (filters.name) {
      params = params.set('name', filters.name);
    }
    if (filters.categoryId) {
      params = params.set('categoryId', filters.categoryId);
    }
    return this.httpClient.get<PdvProductGroup[]>(`${this.apiBaseUrl}/pdv/products`, { params });
  }

  findProductByBarcode(barcode: string): Observable<ProductModel> {
    return this.httpClient.get<ProductModel>(`${this.apiBaseUrl}/pdv/products/barcode/${encodeURIComponent(barcode)}`);
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
