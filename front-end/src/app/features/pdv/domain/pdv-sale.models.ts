import type {
  DecimalString,
  DeliveryType,
  IsoDateTime,
  OrderItemStatus,
  OrderOrigin,
  OrderStatus,
  PaymentMethod,
  PaymentStatus,
  PreparationSector,
  SyncStatus,
  Uuid
} from '../../../shared/models/domain.models';

export interface PdvSaleItem {
  id: Uuid;
  orderId: Uuid;
  productId: Uuid | null;
  productNameSnapshot: string;
  quantity: DecimalString;
  unitPrice: DecimalString;
  totalPrice: DecimalString;
  observation: string | null;
  status: OrderItemStatus;
  preparationSector: PreparationSector;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface PdvSalePayment {
  id: Uuid;
  method: PaymentMethod;
  amount: DecimalString;
}

export interface CreateDiscountPayload {
  amount: string;
}

export interface CancelSalePayload {
  reason: string;
}

export interface PdvSale {
  id: Uuid;
  orderNumber: number | null;
  origin: OrderOrigin;
  status: OrderStatus;
  paymentStatus: PaymentStatus;
  deliveryType: DeliveryType;
  subtotal: DecimalString;
  discountAmount: DecimalString;
  deliveryFee: DecimalString;
  totalAmount: DecimalString;
  items: PdvSaleItem[];
  payments: PdvSalePayment[];
  remainingAmount: DecimalString;
  syncStatus: SyncStatus | null;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface AddPdvSaleItemPayload {
  productId: Uuid;
  quantity?: DecimalString | null;
  observation?: string | null;
}

export interface PatchPdvSaleItemPayload {
  quantity: DecimalString;
}

export interface CreatePaymentPayload {
  method: PaymentMethod;
  amount: DecimalString;
  transactionId?: string | null;
  gateway?: string | null;
}

export interface PaymentResponse {
  id: Uuid;
  orderId: Uuid;
  method: PaymentMethod;
  status: PaymentStatus;
  recordedAmount: DecimalString;
  remainingAmount: DecimalString;
  changeAmount: DecimalString;
  transactionId: string | null;
  gateway: string | null;
  paidAt: IsoDateTime | null;
  canceledAt: IsoDateTime | null;
  orderStatus: OrderStatus;
  orderPaymentStatus: PaymentStatus;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}
