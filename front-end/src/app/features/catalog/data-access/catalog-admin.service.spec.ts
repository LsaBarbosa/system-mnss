import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { mockCategory, mockProduct } from '../../../shared/models/domain.mocks';
import { CatalogAdminService } from './catalog-admin.service';

describe('CatalogAdminService', () => {
  let service: CatalogAdminService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CatalogAdminService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('creates and updates categories through API', () => {
    service.createCategory({ name: 'Paes', showOnPdv: true }).subscribe((category) => {
      expect(category).toEqual(mockCategory);
    });
    const createRequest = httpTestingController.expectOne('/api/categories');
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual({ name: 'Paes', showOnPdv: true });
    createRequest.flush(mockCategory);

    service.updateCategory(mockCategory.id, { name: 'Bolos' }).subscribe((category) => {
      expect(category).toEqual(mockCategory);
    });
    const updateRequest = httpTestingController.expectOne(`/api/categories/${mockCategory.id}`);
    expect(updateRequest.request.method).toBe('PATCH');
    expect(updateRequest.request.body).toEqual({ name: 'Bolos' });
    updateRequest.flush(mockCategory);
  });

  it('uses filters and barcode endpoints for products', () => {
    service.listProducts({ name: 'pao', categoryId: mockCategory.id }).subscribe((products) => {
      expect(products).toEqual([mockProduct]);
    });
    const listRequest = httpTestingController.expectOne(`/api/products?name=pao&categoryId=${mockCategory.id}`);
    expect(listRequest.request.method).toBe('GET');
    listRequest.flush([mockProduct]);

    service.findProductByBarcode('789 100').subscribe((product) => {
      expect(product).toEqual(mockProduct);
    });
    const barcodeRequest = httpTestingController.expectOne('/api/products/barcode/789%20100');
    expect(barcodeRequest.request.method).toBe('GET');
    barcodeRequest.flush(mockProduct);
  });

  it('patches product availability', () => {
    service
      .updateProductAvailability(mockProduct.id, {
        status: 'UNAVAILABLE',
        channel: 'ALL',
        reason: 'Acabou na loja'
      })
      .subscribe((availability) => {
        expect(availability.status).toBe('UNAVAILABLE');
        expect(availability.syncStatus).toBe('PENDING');
      });

    const request = httpTestingController.expectOne(`/api/products/${mockProduct.id}/availability`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({
      status: 'UNAVAILABLE',
      channel: 'ALL',
      reason: 'Acabou na loja'
    });
    request.flush({
      id: 'dddddddd-dddd-dddd-dddd-dddddddddddd',
      productId: mockProduct.id,
      status: 'UNAVAILABLE',
      availableQuantity: null,
      channel: 'ALL',
      reason: 'Acabou na loja',
      updatedBy: null,
      createdAt: '2026-05-04T12:00:00Z',
      updatedAt: '2026-05-04T12:00:00Z',
      syncStatus: 'PENDING'
    });
  });
});
