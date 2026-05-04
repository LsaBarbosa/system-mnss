import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import type { AuditLogModel } from '../models/domain.models';

@Component({
  selector: 'mnss-audit-log-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './audit-log-table.component.html',
  styleUrl: './audit-log-table.component.scss'
})
export class AuditLogTableComponent {
  @Input() logs: AuditLogModel[] = [];

  trackById(_: number, log: AuditLogModel): string {
    return log.id;
  }
}
