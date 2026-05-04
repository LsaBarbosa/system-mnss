import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import type { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { CategoryModel, ProductModel } from '../../../shared/models/domain.models';

export interface PdvProductGroup {
  category: CategoryModel;
  products: ProductModel[];
}

@Injectable({ providedIn: 'root' })
export class PdvCatalogService {
  constructor(private readonly httpClient: HttpClient) {}

  listProducts(): Observable<PdvProductGroup[]> {
    return this.httpClient.get<PdvProductGroup[]>(`${this.apiBaseUrl}/pdv/products`);
  }

  private get apiBaseUrl(): string {
    const runtime: string = environment.runtime;
    return runtime === 'online' ? environment.onlineApiBaseUrl : environment.localApiBaseUrl;
  }
}
