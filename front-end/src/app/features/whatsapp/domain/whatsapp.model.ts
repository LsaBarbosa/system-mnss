import { DeliveryType } from '../../../core/models/order.model';

export enum ConversationStatus {
  OPEN = 'OPEN',
  ASSIGNED = 'ASSIGNED',
  CLOSED = 'CLOSED'
}

export enum MessageDirection {
  INBOUND = 'INBOUND',
  OUTBOUND = 'OUTBOUND'
}

export enum MessageStatus {
  RECEIVED = 'RECEIVED',
  SENT = 'SENT',
  FAILED = 'FAILED'
}

export interface ConversationResponse {
  id: string;
  customerPhone: string;
  customerName: string;
  status: ConversationStatus;
  assignedTo?: string;
  orderId?: string;
  lastMessage?: string;
  createdAt: string;
  updatedAt: string;
}

export interface MessageResponse {
  id: string;
  direction: MessageDirection;
  senderPhone?: string;
  senderName?: string;
  content: string;
  messageType: string;
  status: MessageStatus;
  createdAt: string;
}

export interface WhatsAppOrderItemRequest {
  productId: string;
  quantity?: number;
  observation?: string;
}

export interface CreateWhatsAppOrderRequest {
  conversationId: string;
  customerPhone: string;
  customerName: string;
  customerEmail?: string;
  deliveryType: DeliveryType;
  items: WhatsAppOrderItemRequest[];
  notes?: string;
}
