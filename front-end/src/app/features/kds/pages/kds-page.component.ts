import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KdsService, KdsTicket, KdsTicketStatus } from '../data-access/kds.service';
import { interval, map, startWith } from 'rxjs';

@Component({
  selector: 'app-kds-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kds-page.component.html',
  styleUrl: './kds-page.component.scss'
})
export class KdsPageComponent implements OnInit {
  private kdsService = inject(KdsService);
  
  ticketsWaiting$ = this.kdsService.getTicketsByStatus(KdsTicketStatus.WAITING);
  ticketsPrep$ = this.kdsService.getTicketsByStatus(KdsTicketStatus.IN_PREPARATION);
  ticketsReady$ = this.kdsService.getTicketsByStatus(KdsTicketStatus.READY);
  
  connectionStatus$ = this.kdsService.connectionStatus$;
  currentTime$ = interval(1000).pipe(
    startWith(0),
    map(() => new Date())
  );
  
  selectedSector: string = '';
  sectors = ['CHAPA', 'BEBIDAS', 'CONFEITARIA', 'BALCAO', 'EXPEDICAO', 'DELIVERY'];

  ngOnInit() {
    this.kdsService.loadTickets();
  }

  onSectorChange(sector: string) {
    this.selectedSector = sector;
    this.kdsService.loadTickets(sector);
  }

  getWaitTime(createdAt: string): number {
    const created = new Date(createdAt).getTime();
    const now = new Date().getTime();
    return Math.floor((now - created) / 60000);
  }

  getWaitTimeColor(minutes: number): string {
    if (minutes > 15) return 'var(--danger-color)';
    if (minutes > 10) return 'var(--warning-color)';
    return 'var(--success-color)';
  }

  onStart(id: string) {
    this.kdsService.startTicket(id).subscribe();
  }

  onReadyTicket(id: string) {
    this.kdsService.readyTicket(id).subscribe();
  }

  onReadyItem(id: string) {
    this.kdsService.readyItem(id).subscribe();
  }

  onFinish(id: string) {
    // If it's a ticket id, we use finishTicket, if it's order id, we use finishOrder
    // For now, let's assume it's ticket finish as before, but add finishOrder
    this.kdsService.finishTicket(id).subscribe();
  }

  onFinishOrder(orderId: string) {
    this.kdsService.finishOrder(orderId).subscribe();
  }
}
