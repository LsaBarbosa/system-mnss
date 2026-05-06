import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { 
  ConversationResponse, 
  MessageResponse, 
  CreateWhatsAppOrderRequest 
} from '../domain/whatsapp.model';
import { PublicProductResponse } from '../../site-publico/domain/public-menu.model';
import { OrderResponse } from '../../../core/models/order.model';

@Injectable({
  providedIn: 'root'
})
export class WhatsAppService {
  private apiUrl = `${environment.onlineApiBaseUrl}/api/whatsapp`;

  constructor(private http: HttpClient) {}

  getConversations(): Observable<ConversationResponse[]> {
    return this.http.get<ConversationResponse[]>(`${this.apiUrl}/conversations`);
  }

  getMessages(conversationId: string): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/conversations/${conversationId}/messages`);
  }

  sendMessage(conversationId: string, content: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/conversations/${conversationId}/messages`, { content });
  }

  assignConversation(conversationId: string, userId: string): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(`${this.apiUrl}/conversations/${conversationId}/assign`, { userId });
  }

  getCatalog(): Observable<PublicProductResponse[]> {
    return this.http.get<PublicProductResponse[]>(`${this.apiUrl}/catalog`);
  }

  createOrder(request: CreateWhatsAppOrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.apiUrl}/orders`, request);
  }
}
