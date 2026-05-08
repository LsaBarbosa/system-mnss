import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SyncService, SyncEvent, SyncStatusCounts } from '../../data-access/sync.service';

@Component({
  selector: 'app-sync-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sync-dashboard.component.html',
  styleUrl: './sync-dashboard.component.scss'
})
export class SyncDashboardComponent implements OnInit {
  statusCounts: SyncStatusCounts = {};
  events: SyncEvent[] = [];

  showPayloadModal = false;
  selectedPayload: string = '';

  showIgnoreModal = false;
  ignoreReason = '';
  private pendingIgnoreEvent: SyncEvent | null = null;

  constructor(private syncService: SyncService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.syncService.getSyncStatus().subscribe((counts) => (this.statusCounts = counts));
    this.syncService.getEvents().subscribe((events) => (this.events = events));
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'SYNCED':
        return 'status-synced';
      case 'RECEIVED_BY_STORE':
        return 'status-received';
      case 'FAILED':
        return 'status-failed';
      case 'RETRYING':
        return 'status-retrying';
      case 'DEAD_LETTER':
        return 'status-dead-letter';
      case 'PROCESSING':
        return 'status-processing';
      case 'PENDING':
        return 'status-pending';
      case 'IGNORED':
        return 'status-ignored';
      default:
        return 'status-unknown';
    }
  }

  reprocess(event: SyncEvent): void {
    this.syncService.reprocessEvent(event.id).subscribe(() => this.loadData());
  }

  openIgnoreModal(event: SyncEvent): void {
    this.pendingIgnoreEvent = event;
    this.ignoreReason = '';
    this.showIgnoreModal = true;
  }

  confirmIgnore(): void {
    if (this.pendingIgnoreEvent && this.ignoreReason.trim()) {
      this.syncService.ignoreEvent(this.pendingIgnoreEvent.id, this.ignoreReason.trim()).subscribe(() => {
        this.loadData();
        this.closeIgnoreModal();
      });
    }
  }

  closeIgnoreModal(): void {
    this.showIgnoreModal = false;
    this.pendingIgnoreEvent = null;
    this.ignoreReason = '';
  }

  viewPayload(event: SyncEvent): void {
    this.selectedPayload = JSON.stringify(event.payload, null, 2);
    this.showPayloadModal = true;
  }

  closePayloadModal(): void {
    this.showPayloadModal = false;
    this.selectedPayload = '';
  }
}
