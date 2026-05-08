import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConversationResponse } from '../../domain/whatsapp.model';

@Component({
  selector: 'app-conversation-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="conv-list">
      <div
        *ngFor="let conv of conversations"
        class="conv-item"
        [class.selected]="selectedId === conv.id"
        (click)="selected.emit(conv)"
      >
        <div class="conv-avatar">
          {{ (conv.customerName || 'C')[0] }}
        </div>

        <div class="conv-info">
          <div class="conv-top-row">
            <span class="conv-name">{{ conv.customerName || conv.customerPhone }}</span>
            <span class="conv-time">{{ conv.updatedAt | date: 'HH:mm' }}</span>
          </div>

          <div class="conv-bottom-row">
            <span class="conv-preview">{{ conv.lastMessage || 'Sem mensagens' }}</span>
            <span class="conv-status" [ngClass]="conv.status.toLowerCase()"></span>
          </div>
        </div>
      </div>

      <div *ngIf="conversations.length === 0" class="empty-list">Nenhuma conversa</div>
    </div>
  `,
  styles: [
    `
      .conv-list {
        display: flex;
        flex-direction: column;
      }

      .conv-item {
        display: flex;
        padding: 14px 20px;
        gap: 12px;
        cursor: pointer;
        border-bottom: 1px solid #f0f3f5;
        transition: background 0.2s;

        &:hover {
          background: #f7f9fa;
        }

        &.selected {
          background: #f0f7ff;
          border-left: 4px solid #f1b74a;
          padding-left: 16px;
        }
      }

      .conv-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #eef2f4;
        color: #5a6970;
        display: grid;
        place-items: center;
        font-weight: 700;
        flex-shrink: 0;
      }

      .conv-info {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        justify-content: center;
      }

      .conv-top-row,
      .conv-bottom-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 8px;
      }

      .conv-name {
        font-weight: 600;
        color: #233239;
        font-size: 0.95rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .conv-time {
        font-size: 0.75rem;
        color: #8fa0a8;
        flex-shrink: 0;
      }

      .conv-preview {
        font-size: 0.85rem;
        color: #5a6970;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .conv-status {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        flex-shrink: 0;

        &.open {
          background: #c69a22;
        }
        &.assigned {
          background: #1d8f62;
        }
        &.closed {
          background: #b8c8ce;
        }
      }

      .empty-list {
        padding: 40px 20px;
        text-align: center;
        color: #8fa0a8;
        font-style: italic;
      }
    `
  ]
})
export class ConversationListComponent {
  @Input() conversations: ConversationResponse[] = [];
  @Input() selectedId?: string;
  @Output() selected = new EventEmitter<ConversationResponse>();
}
