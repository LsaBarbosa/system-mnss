import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SyncService, SyncEvent, SyncStatusCounts } from '../../data-access/sync.service';

@Component({
  selector: 'app-sync-dashboard',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './sync-dashboard.component.html',
  styleUrl: './sync-dashboard.component.scss'
})
export class SyncDashboardComponent implements OnInit {
  statusCounts: SyncStatusCounts = {};
  events: SyncEvent[] = [];

  constructor(private syncService: SyncService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.syncService.getSyncStatus().subscribe(counts => this.statusCounts = counts);
    this.syncService.getEvents().subscribe(events => this.events = events);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'SYNCED': return 'status-synced';
      case 'RECEIVED_BY_STORE': return 'status-received';
      case 'FAILED': return 'status-failed';
      case 'PENDING': return 'status-pending';
      case 'IGNORED': return 'status-ignored';
      default: return '';
    }
  }

  reprocess(event: SyncEvent): void {
    this.syncService.reprocessEvent(event.id).subscribe(() => this.loadData());
  }

  ignore(event: SyncEvent): void {
    const reason = prompt('Motivo para ignorar:');
    if (reason) {
      this.syncService.ignoreEvent(event.id, reason).subscribe(() => this.loadData());
    }
  }

  viewPayload(event: SyncEvent): void {
    alert(JSON.stringify(event.payload, null, 2));
  }
}
