import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { mockCategory, mockProduct } from '../../../shared/models/domain.mocks';
import { PdvCatalogService } from './pdv-catalog.service';

describe('PdvCatalogService', () => {
  let service: PdvCatalogService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PdvCatalogService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('requests sellable PDV products', () => {
    service.listProducts().subscribe((groups) => {
      expect(groups[0].products).toEqual([mockProduct]);
    });

    const request = httpTestingController.expectOne('/api/pdv/products');
    expect(request.request.method).toBe('GET');
    request.flush([{ category: mockCategory, products: [mockProduct] }]);
  });
});
