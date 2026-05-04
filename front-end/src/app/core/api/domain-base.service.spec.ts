import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { mockAuditLog, mockCategory, mockOrder, mockPayment, mockProduct } from '../../shared/models/domain.mocks';
import { DomainBaseService } from './domain-base.service';

describe('DomainBaseService', () => {
  let service: DomainBaseService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(DomainBaseService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('uses correct URL and HTTP method for category contracts', () => {
    service.listCategories().subscribe((categories) => {
      expect(categories).toEqual([mockCategory]);
    });

    const request = httpTestingController.expectOne('/api/categories');
    expect(request.request.method).toBe('GET');
    request.flush([mockCategory]);
  });

  it('uses correct URL and HTTP method for product contracts', () => {
    service.listProducts().subscribe((products) => {
      expect(products).toEqual([mockProduct]);
    });

    const request = httpTestingController.expectOne('/api/products');
    expect(request.request.method).toBe('GET');
    request.flush([mockProduct]);
  });

  it('uses correct URL and HTTP method for order and payment contracts', () => {
    service.listOrders().subscribe((orders) => {
      expect(orders).toEqual([mockOrder]);
    });
    service.listPayments().subscribe((payments) => {
      expect(payments).toEqual([mockPayment]);
    });

    const orderRequest = httpTestingController.expectOne('/api/orders');
    expect(orderRequest.request.method).toBe('GET');
    orderRequest.flush([mockOrder]);

    const paymentRequest = httpTestingController.expectOne('/api/payments');
    expect(paymentRequest.request.method).toBe('GET');
    paymentRequest.flush([mockPayment]);
  });

  it('uses correct URL and HTTP method for audit log contracts', () => {
    service.listAuditLogs().subscribe((logs) => {
      expect(logs).toEqual([mockAuditLog]);
    });

    const request = httpTestingController.expectOne('/api/audit-logs');
    expect(request.request.method).toBe('GET');
    request.flush([mockAuditLog]);
  });
});
