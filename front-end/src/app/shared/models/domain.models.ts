export type Uuid = string;
export type IsoDateTime = string;
export type DateOnly = string;
export type DecimalString = string;

export interface TimestampedResource {
  id: Uuid;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export type UnitType = 'UNIT' | 'KG' | 'GRAM' | 'LITER' | 'ML' | 'SLICE' | 'PORTION' | 'PACKAGE';
export type PreparationSector =
  | 'BALCAO'
  | 'CHAPA'
  | 'BEBIDAS'
  | 'CONFEITARIA'
  | 'EXPEDICAO'
  | 'DELIVERY'
  | 'SEM_PREPARO';
export type SalesChannel = 'PDV' | 'SITE' | 'WHATSAPP' | 'ALL';
export type AvailabilityStatus =
  | 'AVAILABLE'
  | 'UNAVAILABLE'
  | 'AVAILABLE_UNTIL_STOCK_ENDS'
  | 'PRE_ORDER_ONLY'
  | 'PICKUP_ONLY'
  | 'DELIVERY_ONLY'
  | 'COUNTER_ONLY';

export interface CategoryModel extends TimestampedResource {
  name: string;
  description: string | null;
  displayOrder: number;
  imageUrl: string | null;
  active: boolean;
  showOnline: boolean;
  showOnPdv: boolean;
  showOnWhatsapp: boolean;
}

export interface ProductModel extends TimestampedResource {
  categoryId: Uuid | null;
  name: string;
  description: string | null;
  price: DecimalString;
  promotionalPrice: DecimalString | null;
  costPrice: DecimalString | null;
  sku: string | null;
  barcode: string | null;
  imageUrl: string | null;
  unitType: UnitType;
  preparationSector: PreparationSector;
  preparationTimeMinutes: number | null;
  active: boolean;
  available: boolean;
  sellOnPdv: boolean;
  sellOnline: boolean;
  sellOnWhatsapp: boolean;
}

export interface ProductAvailabilityModel extends TimestampedResource {
  productId: Uuid;
  status: AvailabilityStatus;
  availableQuantity: DecimalString | null;
  channel: SalesChannel;
  reason: string | null;
  updatedBy: Uuid | null;
}

export interface CustomerModel extends TimestampedResource {
  name: string;
  phone: string | null;
  email: string | null;
  document: string | null;
  birthDate: DateOnly | null;
}

export interface CustomerAddressModel extends TimestampedResource {
  customerId: Uuid;
  label: string | null;
  street: string;
  number: string | null;
  complement: string | null;
  neighborhood: string | null;
  city: string | null;
  state: string | null;
  zipCode: string | null;
  reference: string | null;
  latitude: DecimalString | null;
  longitude: DecimalString | null;
  defaultAddress: boolean;
}

export type OrderOrigin = 'PDV' | 'SITE' | 'WHATSAPP' | 'ADMIN' | 'MANUAL' | 'INTEGRATION';
export type OrderStatus =
  | 'CREATED'
  | 'PAYMENT_PENDING'
  | 'PAID'
  | 'SENT_TO_STORE'
  | 'RECEIVED_BY_STORE'
  | 'ACCEPTED'
  | 'IN_PREPARATION'
  | 'READY'
  | 'OUT_FOR_DELIVERY'
  | 'DELIVERED'
  | 'FINISHED'
  | 'CANCELED';
export type DeliveryType = 'COUNTER' | 'PICKUP' | 'DELIVERY' | 'LOCAL_CONSUMPTION';
export type OrderItemStatus = 'CREATED' | 'WAITING_PREPARATION' | 'IN_PREPARATION' | 'READY' | 'DELIVERED' | 'CANCELED';

export interface OrderModel extends TimestampedResource {
  orderNumber: number | null;
  customerId: Uuid | null;
  origin: OrderOrigin;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  deliveryType: DeliveryType;
  subtotal: DecimalString;
  discountAmount: DecimalString;
  deliveryFee: DecimalString;
  totalAmount: DecimalString;
  notes: string | null;
  finishedAt: IsoDateTime | null;
  canceledAt: IsoDateTime | null;
}

export interface OrderItemModel extends TimestampedResource {
  orderId: Uuid;
  productId: Uuid | null;
  productNameSnapshot: string;
  quantity: DecimalString;
  unitPrice: DecimalString;
  totalPrice: DecimalString;
  observation: string | null;
  status: OrderItemStatus;
  preparationSector: PreparationSector;
}

export type PaymentMethod =
  | 'CASH'
  | 'PIX'
  | 'CREDIT_CARD'
  | 'DEBIT_CARD'
  | 'ONLINE_PIX'
  | 'ONLINE_CREDIT_CARD'
  | 'ONLINE_DEBIT_CARD'
  | 'MEAL_VOUCHER'
  | 'MIXED';
export type PaymentStatus = 'PENDING' | 'AUTHORIZED' | 'PAID' | 'REFUSED' | 'CANCELED' | 'REFUNDED' | 'EXPIRED';

export interface PaymentModel extends TimestampedResource {
  orderId: Uuid;
  method: PaymentMethod;
  status: PaymentStatus;
  amount: DecimalString;
  transactionId: string | null;
  gateway: string | null;
  paidAt: IsoDateTime | null;
  canceledAt: IsoDateTime | null;
}

export type CashRegisterStatus = 'OPEN' | 'CLOSED' | 'BLOCKED';
export type CashMovementType = 'SALE' | 'CASH_IN' | 'CASH_OUT' | 'REFUND' | 'ADJUSTMENT';

export interface CashRegisterModel extends TimestampedResource {
  operatorId: Uuid;
  openedAt: IsoDateTime;
  closedAt: IsoDateTime | null;
  openingAmount: DecimalString;
  closingAmount: DecimalString | null;
  expectedAmount: DecimalString | null;
  differenceAmount: DecimalString | null;
  status: CashRegisterStatus;
  notes: string | null;
}

export interface CashMovementModel extends TimestampedResource {
  cashRegisterId: Uuid;
  type: CashMovementType;
  paymentMethod: PaymentMethod | null;
  amount: DecimalString;
  description: string | null;
  orderId: Uuid | null;
  createdBy: Uuid | null;
}

export type KdsTicketStatus = 'WAITING' | 'IN_PREPARATION' | 'READY' | 'FINISHED' | 'CANCELED';

export interface KdsTicketModel extends TimestampedResource {
  orderId: Uuid;
  ticketNumber: number | null;
  sector: PreparationSector;
  status: KdsTicketStatus;
  startedAt: IsoDateTime | null;
  readyAt: IsoDateTime | null;
  finishedAt: IsoDateTime | null;
}

export interface KdsTicketItemModel extends TimestampedResource {
  kdsTicketId: Uuid;
  orderItemId: Uuid;
  status: KdsTicketStatus;
}

export type StockMovementType = 'IN' | 'OUT' | 'ADJUSTMENT' | 'SALE' | 'LOSS' | 'RETURN';

export interface StockMovementModel extends TimestampedResource {
  productId: Uuid;
  type: StockMovementType;
  quantity: DecimalString;
  reason: string | null;
  orderId: Uuid | null;
  createdBy: Uuid | null;
}

export type SyncDirection = 'LOCAL_TO_ONLINE' | 'ONLINE_TO_LOCAL';
export type SyncEnvironment = 'LOCAL' | 'ONLINE';
export type SyncStatus = 'PENDING' | 'PROCESSING' | 'SYNCED' | 'FAILED' | 'RETRYING' | 'IGNORED';

export interface SyncEventModel extends TimestampedResource {
  idempotencyKey: string;
  direction: SyncDirection;
  sourceEnvironment: SyncEnvironment;
  targetEnvironment: SyncEnvironment;
  aggregateType: string;
  aggregateId: Uuid | null;
  eventType: string;
  payload: Record<string, unknown>;
  status: SyncStatus;
  retryCount: number;
  nextRetryAt: IsoDateTime | null;
  lastError: string | null;
  processedAt: IsoDateTime | null;
}

export interface AuditLogModel extends TimestampedResource {
  actorUserId: Uuid | null;
  action: string;
  entityType: string;
  entityId: Uuid | null;
  details: Record<string, unknown>;
  ipAddress: string | null;
}
