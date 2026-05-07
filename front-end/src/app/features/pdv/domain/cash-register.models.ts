import type {
  CashMovementModel,
  CashMovementType,
  CashRegisterModel,
  DecimalString,
  PaymentMethod,
  Uuid
} from '../../../shared/models/domain.models';

export interface CashRegisterOpenPayload {
  openingAmount: DecimalString;
  notes?: string | null;
}

export interface CashRegisterClosePayload {
  closingAmount: DecimalString;
  notes?: string | null;
}

export interface CashMovementPayload {
  type: CashMovementType;
  paymentMethod?: PaymentMethod | null;
  amount: DecimalString;
  description?: string | null;
  orderId?: Uuid | null;
}

export type CashMovementResponse = CashMovementModel;

export interface CurrentCashRegisterResponse {
  open: boolean;
  cashRegister: CashRegisterModel | null;
}

export interface CashRegisterSummaryResponse {
  cashRegister: CashRegisterModel;
  totalsByPaymentMethod: Record<PaymentMethod, DecimalString>;
  saleTotal: DecimalString;
  refundTotal: DecimalString;
  cashInTotal: DecimalString;
  cashOutTotal: DecimalString;
  adjustmentTotal: DecimalString;
  expectedAmount: DecimalString;
  closingAmount: DecimalString | null;
  differenceAmount: DecimalString;
  movements: CashMovementResponse[];
}
