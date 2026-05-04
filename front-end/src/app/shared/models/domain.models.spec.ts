import {
  mockAuditLog,
  mockCashRegister,
  mockCategory,
  mockCustomer,
  mockKdsTicket,
  mockOrder,
  mockPayment,
  mockProduct,
  mockSyncEvent
} from './domain.mocks';
import type {
  AuditLogModel,
  CashRegisterModel,
  CategoryModel,
  CustomerModel,
  KdsTicketModel,
  OrderModel,
  PaymentModel,
  ProductModel,
  SyncEventModel
} from './domain.models';

describe('domain model contracts', () => {
  it('compiles required TypeScript fields for core aggregates', () => {
    const category: CategoryModel = mockCategory;
    const product: ProductModel = mockProduct;
    const customer: CustomerModel = mockCustomer;
    const order: OrderModel = mockOrder;
    const payment: PaymentModel = mockPayment;
    const cashRegister: CashRegisterModel = mockCashRegister;
    const kdsTicket: KdsTicketModel = mockKdsTicket;
    const syncEvent: SyncEventModel = mockSyncEvent;
    const auditLog: AuditLogModel = mockAuditLog;

    expect(category.id).toBeTruthy();
    expect(product.price).toBe('1.20');
    expect(customer.name).toBeTruthy();
    expect(order.totalAmount).toBe('12.00');
    expect(payment.method).toBe('PIX');
    expect(cashRegister.status).toBe('OPEN');
    expect(kdsTicket.sector).toBe('CHAPA');
    expect(syncEvent.payload['name']).toBe('Pao frances');
    expect(auditLog.action).toBe('PRODUCT_PRICE_CHANGED');
  });
});
