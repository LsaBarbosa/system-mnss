import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type {
  AvailabilityStatus,
  CategoryModel,
  PreparationSector,
  ProductAvailabilityModel,
  ProductModel,
  SalesChannel,
  SyncStatus,
  UnitType,
  Uuid
} from '../../../shared/models/domain.models';

export interface CategoryPayload {
  name: string;
  description?: string | null;
  displayOrder?: number | null;
  imageUrl?: string | null;
  active?: boolean | null;
  showOnline?: boolean | null;
  showOnPdv?: boolean | null;
  showOnWhatsapp?: boolean | null;
}

export interface ProductPayload {
  categoryId: Uuid;
  name: string;
  description?: string | null;
  price: string;
  promotionalPrice?: string | null;
  costPrice?: string | null;
  sku?: string | null;
  barcode?: string | null;
  imageUrl?: string | null;
  unitType: UnitType;
  preparationSector: PreparationSector;
  preparationTimeMinutes?: number | null;
  active?: boolean | null;
  available?: boolean | null;
  sellOnPdv?: boolean | null;
  sellOnline?: boolean | null;
  sellOnWhatsapp?: boolean | null;
}

export interface ProductFilters {
  name?: string | null;
  categoryId?: Uuid | null;
}

export interface ProductAvailabilityPayload {
  status: AvailabilityStatus;
  channel?: SalesChannel | null;
  availableQuantity?: string | null;
  reason?: string | null;
}

export interface ProductAvailabilityChange extends ProductAvailabilityModel {
  syncStatus: SyncStatus;
}

@Injectable({ providedIn: 'root' })
export class CatalogAdminService {
  constructor(private readonly httpClient: HttpClient) {}

  listCategories(channel?: 'ADMIN' | 'PDV'): Observable<CategoryModel[]> {
    const params = channel ? new HttpParams().set('channel', channel) : undefined;
    return this.httpClient.get<CategoryModel[]>(`${this.apiBaseUrl}/categories`, { params });
  }

  createCategory(payload: CategoryPayload): Observable<CategoryModel> {
    return this.httpClient.post<CategoryModel>(`${this.apiBaseUrl}/categories`, payload);
  }

  updateCategory(id: Uuid, payload: Partial<CategoryPayload>): Observable<CategoryModel> {
    return this.httpClient.patch<CategoryModel>(`${this.apiBaseUrl}/categories/${id}`, payload);
  }

  listProducts(filters: ProductFilters = {}): Observable<ProductModel[]> {
    let params = new HttpParams();
    if (filters.name) {
      params = params.set('name', filters.name);
    }
    if (filters.categoryId) {
      params = params.set('categoryId', filters.categoryId);
    }
    return this.httpClient.get<ProductModel[]>(`${this.apiBaseUrl}/products`, { params });
  }

  createProduct(payload: ProductPayload): Observable<ProductModel> {
    return this.httpClient.post<ProductModel>(`${this.apiBaseUrl}/products`, payload);
  }

  updateProduct(id: Uuid, payload: Partial<ProductPayload>): Observable<ProductModel> {
    return this.httpClient.patch<ProductModel>(`${this.apiBaseUrl}/products/${id}`, payload);
  }

  updateProductAvailability(id: Uuid, payload: ProductAvailabilityPayload): Observable<ProductAvailabilityChange> {
    return this.httpClient.patch<ProductAvailabilityChange>(`${this.apiBaseUrl}/products/${id}/availability`, payload);
  }

  findProductByBarcode(barcode: string): Observable<ProductModel> {
    return this.httpClient.get<ProductModel>(`${this.apiBaseUrl}/products/barcode/${encodeURIComponent(barcode)}`);
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
