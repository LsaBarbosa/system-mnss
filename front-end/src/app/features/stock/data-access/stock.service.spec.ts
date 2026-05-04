import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { mockProduct, mockStockMovement } from '../../../shared/models/domain.mocks';
import { StockService } from './stock.service';

describe('StockService', () => {
  let service: StockService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(StockService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('registers stock movement through API', () => {
    service
      .createMovement({
        productId: mockProduct.id,
        type: 'LOSS',
        quantity: '1.000',
        reason: 'Quebra'
      })
      .subscribe((movement) => {
        expect(movement.type).toBe('LOSS');
        expect(movement.productName).toBe(mockProduct.name);
      });

    const request = httpTestingController.expectOne('/api/stock-movements');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      productId: mockProduct.id,
      type: 'LOSS',
      quantity: '1.000',
      reason: 'Quebra'
    });
    request.flush({ ...mockStockMovement, type: 'LOSS', productName: mockProduct.name });
  });

  it('loads total stock balance by product', () => {
    service.listBalances().subscribe((balances) => {
      expect(balances).toEqual([{ productId: mockProduct.id, productName: mockProduct.name, quantity: '10.000' }]);
    });

    const request = httpTestingController.expectOne('/api/stock-movements/balances');
    expect(request.request.method).toBe('GET');
    request.flush([{ productId: mockProduct.id, productName: mockProduct.name, quantity: '10.000' }]);
  });

  it('loads movements with product filter', () => {
    service.listMovements(mockProduct.id).subscribe((movements) => {
      expect(movements[0].productId).toBe(mockProduct.id);
    });

    const request = httpTestingController.expectOne(`/api/stock-movements?productId=${mockProduct.id}`);
    expect(request.request.method).toBe('GET');
    request.flush([{ ...mockStockMovement, productName: mockProduct.name }]);
  });
});
