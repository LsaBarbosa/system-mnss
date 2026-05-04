import type {
  AuditLogModel,
  CashMovementModel,
  CashRegisterModel,
  CategoryModel,
  CustomerModel,
  KdsTicketModel,
  OrderModel,
  PaymentModel,
  ProductModel,
  StockMovementModel,
  SyncEventModel
} from './domain.models';

const createdAt = '2026-05-04T12:00:00Z';
const updatedAt = '2026-05-04T12:05:00Z';

export const mockCategory: CategoryModel = {
  id: '11111111-1111-1111-1111-111111111111',
  name: 'Paes',
  description: 'Produtos de padaria',
  displayOrder: 1,
  imageUrl: null,
  active: true,
  showOnline: true,
  showOnPdv: true,
  showOnWhatsapp: true,
  createdAt,
  updatedAt
};

export const mockProduct: ProductModel = {
  id: '22222222-2222-2222-2222-222222222222',
  categoryId: mockCategory.id,
  name: 'Pao frances',
  description: null,
  price: '1.20',
  promotionalPrice: null,
  costPrice: '0.50',
  sku: 'PAO-FRANCES',
  barcode: null,
  imageUrl: null,
  unitType: 'UNIT',
  preparationSector: 'SEM_PREPARO',
  preparationTimeMinutes: null,
  active: true,
  available: true,
  sellOnPdv: true,
  sellOnline: true,
  sellOnWhatsapp: true,
  createdAt,
  updatedAt
};

export const mockCustomer: CustomerModel = {
  id: '33333333-3333-3333-3333-333333333333',
  name: 'Cliente Online',
  phone: '11999990000',
  email: 'cliente@example.com',
  document: null,
  birthDate: null,
  createdAt,
  updatedAt
};

export const mockOrder: OrderModel = {
  id: '44444444-4444-4444-4444-444444444444',
  orderNumber: 12,
  customerId: mockCustomer.id,
  origin: 'SITE',
  status: 'CREATED',
  paymentStatus: 'PENDING',
  deliveryType: 'PICKUP',
  subtotal: '12.00',
  discountAmount: '0.00',
  deliveryFee: '0.00',
  totalAmount: '12.00',
  notes: null,
  finishedAt: null,
  canceledAt: null,
  createdAt,
  updatedAt
};

export const mockPayment: PaymentModel = {
  id: '55555555-5555-5555-5555-555555555555',
  orderId: mockOrder.id,
  method: 'PIX',
  status: 'PENDING',
  amount: '12.00',
  transactionId: null,
  gateway: null,
  paidAt: null,
  canceledAt: null,
  createdAt,
  updatedAt
};

export const mockCashRegister: CashRegisterModel = {
  id: '66666666-6666-6666-6666-666666666666',
  operatorId: '77777777-7777-7777-7777-777777777777',
  openedAt: createdAt,
  closedAt: null,
  openingAmount: '100.00',
  closingAmount: null,
  expectedAmount: null,
  differenceAmount: null,
  status: 'OPEN',
  notes: null,
  createdAt,
  updatedAt
};

export const mockCashMovement: CashMovementModel = {
  id: '67676767-6767-6767-6767-676767676767',
  cashRegisterId: mockCashRegister.id,
  type: 'CASH_IN',
  paymentMethod: null,
  amount: '15.00',
  description: 'Suprimento',
  orderId: null,
  createdBy: mockCashRegister.operatorId,
  createdAt,
  updatedAt
};

export const mockKdsTicket: KdsTicketModel = {
  id: '88888888-8888-8888-8888-888888888888',
  orderId: mockOrder.id,
  ticketNumber: 6,
  sector: 'CHAPA',
  status: 'WAITING',
  startedAt: null,
  readyAt: null,
  finishedAt: null,
  createdAt,
  updatedAt
};

export const mockStockMovement: StockMovementModel = {
  id: '12121212-1212-1212-1212-121212121212',
  productId: mockProduct.id,
  type: 'IN',
  quantity: '10.000',
  reason: null,
  orderId: null,
  createdBy: mockCashRegister.operatorId,
  createdAt,
  updatedAt
};

export const mockSyncEvent: SyncEventModel = {
  id: '99999999-9999-9999-9999-999999999999',
  idempotencyKey: 'product-22222222-price-20260504',
  direction: 'LOCAL_TO_ONLINE',
  sourceEnvironment: 'LOCAL',
  targetEnvironment: 'ONLINE',
  aggregateType: 'Product',
  aggregateId: mockProduct.id,
  eventType: 'PRODUCT_UPDATED',
  payload: { name: mockProduct.name },
  status: 'PENDING',
  retryCount: 0,
  nextRetryAt: null,
  lastError: null,
  processedAt: null,
  createdAt,
  updatedAt
};

export const mockAuditLog: AuditLogModel = {
  id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  actorUserId: mockCashRegister.operatorId,
  action: 'PRODUCT_PRICE_CHANGED',
  entityType: 'Product',
  entityId: mockProduct.id,
  details: { newPrice: '1.20' },
  ipAddress: '127.0.0.1',
  createdAt,
  updatedAt
};

export const mockAvailabilityAuditLog: AuditLogModel = {
  id: 'abababab-abab-abab-abab-abababababab',
  actorUserId: mockCashRegister.operatorId,
  action: 'PRODUCT_AVAILABILITY_CHANGED',
  entityType: 'ProductAvailability',
  entityId: mockProduct.id,
  details: {
    oldValue: { status: 'AVAILABLE' },
    newValue: { status: 'UNAVAILABLE' }
  },
  ipAddress: '127.0.0.1',
  createdAt,
  updatedAt
};
