export type DeliveryType = 'PICKUP' | 'DELIVERY';

export interface CustomerRequest {
  name: string;
  phone: string;
  email?: string;
  document?: string;
}

export interface AddressRequest {
  street: string;
  number?: string;
  neighborhood?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  complement?: string;
  reference?: string;
}

export interface OrderItemRequest {
  productId: string;
  quantity: number;
  observation?: string;
}

export interface CreateOrderRequest {
  customer: CustomerRequest;
  deliveryType: DeliveryType;
  address?: AddressRequest;
  items: OrderItemRequest[];
  notes?: string;
  paymentMethod: string;
}

export interface OrderResponse {
  id: string;
  orderNumber: number;
  status: string;
  paymentStatus: string;
  deliveryType: string;
  paymentMethod: string;
  subtotal: number;
  discountAmount: number;
  deliveryFee: number;
  totalAmount: number;
}
