import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { mockOrder, mockProduct } from '../../../shared/models/domain.mocks';
import { PdvSaleService } from './pdv-sale.service';
import type { PdvSale } from './pdv-sale.service';

describe('PdvSaleService', () => {
  let service: PdvSaleService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PdvSaleService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('creates sale and manages cart endpoints', () => {
    service.listSales().subscribe((sales) => {
      expect(sales.length).toBe(1);
    });
    const listRequest = httpTestingController.expectOne('/api/pdv/sales');
    expect(listRequest.request.method).toBe('GET');
    listRequest.flush([sale()]);

    service.createSale().subscribe((sale) => {
      expect(sale.status).toBe('CREATED');
    });
    const createRequest = httpTestingController.expectOne('/api/pdv/sales');
    expect(createRequest.request.method).toBe('POST');
    createRequest.flush(sale());

    service.addItem(mockOrder.id, { productId: mockProduct.id, quantity: '1.000' }).subscribe((saleResponse) => {
      expect(saleResponse.items.length).toBe(1);
    });
    const addRequest = httpTestingController.expectOne(`/api/pdv/sales/${mockOrder.id}/items`);
    expect(addRequest.request.method).toBe('POST');
    expect(addRequest.request.body).toEqual({ productId: mockProduct.id, quantity: '1.000' });
    addRequest.flush(sale());

    service.updateItem(mockOrder.id, 'item-id', { quantity: '2.000' }).subscribe((saleResponse) => {
      expect(saleResponse.totalAmount).toBe('2.40');
    });
    const patchRequest = httpTestingController.expectOne(`/api/pdv/sales/${mockOrder.id}/items/item-id`);
    expect(patchRequest.request.method).toBe('PATCH');
    expect(patchRequest.request.body).toEqual({ quantity: '2.000' });
    patchRequest.flush({ ...sale(), subtotal: '2.40', totalAmount: '2.40' });

    service.removeItem(mockOrder.id, 'item-id').subscribe((saleResponse) => {
      expect(saleResponse.items).toEqual([]);
    });
    const deleteRequest = httpTestingController.expectOne(`/api/pdv/sales/${mockOrder.id}/items/item-id`);
    expect(deleteRequest.request.method).toBe('DELETE');
    deleteRequest.flush({ ...sale(), items: [] });

    service.pay(mockOrder.id, { method: 'PIX', amount: '2.40', transactionId: 'tx-1' }).subscribe((payment) => {
      expect(payment.orderPaymentStatus).toBe('PAID');
    });
    const paymentRequest = httpTestingController.expectOne(`/api/pdv/sales/${mockOrder.id}/payment`);
    expect(paymentRequest.request.method).toBe('POST');
    expect(paymentRequest.request.body).toEqual({ method: 'PIX', amount: '2.40', transactionId: 'tx-1' });
    paymentRequest.flush({
      id: 'payment-id',
      orderId: mockOrder.id,
      method: 'PIX',
      status: 'PAID',
      recordedAmount: '2.40',
      remainingAmount: '0.00',
      changeAmount: '0.00',
      transactionId: 'tx-1',
      gateway: null,
      paidAt: mockOrder.updatedAt,
      canceledAt: null,
      orderStatus: 'PAID',
      orderPaymentStatus: 'PAID',
      createdAt: mockOrder.createdAt,
      updatedAt: mockOrder.updatedAt
    });

    service.finish(mockOrder.id).subscribe((saleResponse) => {
      expect(saleResponse.status).toBe('FINISHED');
    });
    const finishRequest = httpTestingController.expectOne(`/api/pdv/sales/${mockOrder.id}/finish`);
    expect(finishRequest.request.method).toBe('POST');
    finishRequest.flush({ ...sale(), status: 'FINISHED' });

    service.applyDiscount(mockOrder.id, { amount: '1.00' }).subscribe((saleResponse) => {
      expect(saleResponse.discountAmount).toBe('1.00');
    });
    const discountRequest = httpTestingController.expectOne(`/api/pdv/sales/${mockOrder.id}/discount`);
    expect(discountRequest.request.method).toBe('POST');
    discountRequest.flush({ ...sale(), discountAmount: '1.00' });

    service.cancelSale(mockOrder.id, { reason: 'Teste' }).subscribe((saleResponse) => {
      expect(saleResponse.status).toBe('CANCELED');
    });
    const cancelRequest = httpTestingController.expectOne(`/api/pdv/sales/${mockOrder.id}/cancel`);
    expect(cancelRequest.request.method).toBe('POST');
    cancelRequest.flush({ ...sale(), status: 'CANCELED' });
  });

  function sale(): PdvSale {
    return {
      id: mockOrder.id,
      orderNumber: mockOrder.orderNumber,
      origin: 'PDV',
      status: 'CREATED',
      paymentStatus: 'PENDING',
      deliveryType: 'LOCAL_CONSUMPTION',
      subtotal: '1.20',
      discountAmount: '0.00',
      deliveryFee: '0.00',
      totalAmount: '1.20',
      payments: [],
      remainingAmount: '1.20',
      createdAt: mockOrder.createdAt,
      updatedAt: mockOrder.updatedAt,
      items: [
        {
          id: 'item-id',
          orderId: mockOrder.id,
          productId: mockProduct.id,
          productNameSnapshot: mockProduct.name,
          quantity: '1.000',
          unitPrice: '1.20',
          totalPrice: '1.20',
          observation: null,
          status: 'CREATED',
          preparationSector: 'SEM_PREPARO',
          createdAt: mockOrder.createdAt,
          updatedAt: mockOrder.updatedAt
        }
      ]
    };
  }
});
