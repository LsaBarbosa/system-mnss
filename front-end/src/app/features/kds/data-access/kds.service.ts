import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client } from '@stomp/stompjs';
import { environment } from '../../../../environments/environment';
import SockJS from 'sockjs-client';
import { BehaviorSubject, Observable, Subject, map } from 'rxjs';

export enum KdsTicketStatus {
  WAITING = 'WAITING',
  IN_PREPARATION = 'IN_PREPARATION',
  READY = 'READY',
  FINISHED = 'FINISHED',
  CANCELED = 'CANCELED'
}

export interface KdsTicketItem {
  id: string;
  productName: string;
  quantity: number;
  observation: string;
  status: KdsTicketStatus;
}

export interface KdsTicket {
  id: string;
  orderId: string;
  ticketNumber: number;
  origin: string;
  sector: string;
  status: KdsTicketStatus;
  createdAt: string;
  startedAt?: string;
  readyAt?: string;
  items: KdsTicketItem[];
}

@Injectable({
  providedIn: 'root'
})
export class KdsService {
  private http = inject(HttpClient);
  private stompClient: Client | null = null;
  private ticketsSubject = new BehaviorSubject<KdsTicket[]>([]);
  tickets$ = this.ticketsSubject.asObservable();

  private readyOrdersSubject = new Subject<string>();
  readyOrders$ = this.readyOrdersSubject.asObservable();

  private connectionStatusSubject = new BehaviorSubject<boolean>(false);
  connectionStatus$ = this.connectionStatusSubject.asObservable();

  constructor() {
    this.initWebSocket();
  }

  private initWebSocket() {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws-kds') as unknown as WebSocket,
      debug: (str) => this.debug(str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.stompClient.onConnect = (frame) => {
      this.connectionStatusSubject.next(true);
      this.debug('Connected: ' + frame);
      this.stompClient?.subscribe('/topic/kds/tickets', (message) => {
        const ticket: KdsTicket = JSON.parse(message.body);
        this.addOrUpdateTicket(ticket);
      });

      this.stompClient?.subscribe('/topic/orders/ready', (message) => {
        this.readyOrdersSubject.next(message.body);
      });
    };

    this.stompClient.onDisconnect = () => {
      this.connectionStatusSubject.next(false);
      this.debug('Disconnected');
    };

    this.stompClient.onStompError = (frame) => {
      this.connectionStatusSubject.next(false);
      this.error('Broker reported error: ' + frame.headers['message']);
      this.error('Additional details: ' + frame.body);
    };

    this.stompClient.activate();
  }

  loadTickets(sector?: string): void {
    const url = sector ? `/api/kds/tickets?sector=${sector}` : '/api/kds/tickets';
    this.http.get<KdsTicket[]>(url).subscribe((tickets) => {
      this.ticketsSubject.next(tickets);
    });
  }

  private addOrUpdateTicket(ticket: KdsTicket) {
    const current = this.ticketsSubject.value;
    const index = current.findIndex((t) => t.id === ticket.id);
    if (index > -1) {
      const updated = [...current];
      updated[index] = ticket;
      this.ticketsSubject.next(updated);
    } else {
      this.ticketsSubject.next([...current, ticket]);
    }
  }

  getTicketsByStatus(status: KdsTicketStatus): Observable<KdsTicket[]> {
    return this.tickets$.pipe(map((tickets) => tickets.filter((t) => t.status === status)));
  }

  startTicket(id: string): Observable<KdsTicket> {
    return this.http.patch<KdsTicket>(`/api/kds/tickets/${id}/start`, {});
  }

  readyTicket(id: string): Observable<KdsTicket> {
    return this.http.patch<KdsTicket>(`/api/kds/tickets/${id}/ready`, {});
  }

  finishTicket(id: string): Observable<KdsTicket> {
    return this.http.patch<KdsTicket>(`/api/kds/tickets/${id}/finish`, {});
  }

  readyItem(id: string): Observable<void> {
    return this.http.patch<void>(`/api/kds/items/${id}/ready`, {});
  }

  finishOrder(id: string): Observable<void> {
    return this.http.patch<void>(`/api/kds/orders/${id}/finish`, {});
  }

  private debug(message: unknown): void {
    if (!environment.production) {
      console.log(message);
    }
  }

  private error(message: unknown): void {
    if (!environment.production) {
      console.error(message);
    }
  }
}
