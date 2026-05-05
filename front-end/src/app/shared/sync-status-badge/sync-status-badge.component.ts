import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import type { SyncStatus } from '../models/domain.models';

@Component({
  selector: 'mnss-sync-status-badge',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sync-status-badge.component.html',
  styleUrl: './sync-status-badge.component.scss'
})
export class SyncStatusBadgeComponent {
  @Input({ required: true }) status: SyncStatus | null | undefined = 'PENDING';

  get config(): { label: string; class: string; tooltip: string; icon: string } {
    switch (this.status) {
      case 'PENDING':
        return {
          label: 'Pendente',
          class: 'warning',
          tooltip: 'Aguardando envio para a nuvem',
          icon: '⏳'
        };
      case 'PROCESSING':
        return {
          label: 'Sincronizando',
          class: 'info',
          tooltip: 'Evento em processamento',
          icon: '🔄'
        };
      case 'SYNCED':
        return {
          label: 'Sincronizado',
          class: 'success',
          tooltip: 'Sincronizado com sucesso',
          icon: '✅'
        };
      case 'FAILED':
        return {
          label: 'Erro Sync',
          class: 'danger',
          tooltip: 'Falha na sincronização',
          icon: '❌'
        };
      case 'RETRYING':
        return {
          label: 'Tentando...',
          class: 'warning',
          tooltip: 'Houve uma falha, tentando novamente',
          icon: '🔁'
        };
      case 'DEAD_LETTER':
        return {
          label: 'Falha Crítica',
          class: 'danger',
          tooltip: 'Sincronização falhou após várias tentativas',
          icon: '🚫'
        };
      case 'IGNORED':
        return {
          label: 'Ignorado',
          class: 'neutral',
          tooltip: 'Evento ignorado por regra de negócio',
          icon: '🔘'
        };
      default:
        return {
          label: 'Desconhecido',
          class: 'neutral',
          tooltip: 'Status de sincronização não definido',
          icon: '❓'
        };
    }
  }
}
