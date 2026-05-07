import { Component, Input, Output, EventEmitter, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MessageResponse } from '../../domain/whatsapp.model';

@Component({
  selector: 'app-chat-view',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chat-viewport">
      <div class="messages-container" #scrollContainer>
        <div *ngFor="let msg of messages" class="msg-row" [ngClass]="msg.direction.toLowerCase()">
          <div class="msg-bubble">
            <div class="msg-text">{{ msg.content }}</div>
            <div class="msg-meta">
              <span class="msg-time">{{ msg.createdAt | date:'HH:mm' }}</span>
              <span *ngIf="msg.direction === 'OUTBOUND'" class="msg-status" [ngClass]="msg.status.toLowerCase()">
                {{ msg.status === 'SENT' ? '✓✓' : '!' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-bar">
        <input type="text" 
               placeholder="Escreva sua mensagem..." 
               [(ngModel)]="newMessage" 
               (keyup.enter)="onSend()">
        <button class="send-btn" (click)="onSend()" [disabled]="!newMessage.trim()">
          Enviar
        </button>
      </div>
    </div>
  `,
  styles: [`
    .chat-viewport {
      display: flex;
      flex-direction: column;
      height: 100%;
      background: #f7f9fa;
    }

    .messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 24px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .msg-row {
      display: flex;
      width: 100%;
    }

    .inbound { justify-content: flex-start; }
    .outbound { justify-content: flex-end; }

    .msg-bubble {
      max-width: 80%;
      padding: 10px 14px;
      border-radius: 12px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05);
      position: relative;
      line-height: 1.4;
    }

    .inbound .msg-bubble {
      background: #ffffff;
      color: #233239;
      border-top-left-radius: 2px;
      border: 1px solid #e1e8eb;
    }

    .outbound .msg-bubble {
      background: #f1b74a;
      color: #172126;
      border-top-right-radius: 2px;
    }

    .msg-text {
      font-size: 0.95rem;
      white-space: pre-wrap;
    }

    .msg-meta {
      display: flex;
      justify-content: flex-end;
      align-items: center;
      gap: 6px;
      margin-top: 4px;
      font-size: 0.7rem;
      opacity: 0.7;
    }

    .msg-status.failed { color: #b54040; font-weight: bold; }

    .input-bar {
      padding: 16px 24px;
      background: #ffffff;
      border-top: 1px solid #e1e8eb;
      display: flex;
      gap: 12px;
    }

    input {
      flex: 1;
      padding: 12px 16px;
      border-radius: 8px;
      border: 1px solid #d4dde1;
      outline: none;
      font-family: inherit;
      transition: border-color 0.2s;

      &:focus {
        border-color: #f1b74a;
      }
    }

    .send-btn {
      padding: 0 24px;
      background: #172126;
      color: #ffffff;
      border: none;
      border-radius: 8px;
      font-weight: 700;
      cursor: pointer;
      transition: opacity 0.2s;

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      &:hover:not(:disabled) {
        opacity: 0.9;
      }
    }
  `]
})
export class ChatViewComponent implements AfterViewChecked {
  @Input() messages: MessageResponse[] = [];
  @Output() sendMessage = new EventEmitter<string>();
  @ViewChild('scrollContainer') private scrollContainer?: ElementRef;

  newMessage = '';

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  onSend() {
    if (this.newMessage.trim()) {
      this.sendMessage.emit(this.newMessage.trim());
      this.newMessage = '';
    }
  }

  private scrollToBottom(): void {
    if (this.scrollContainer) {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    }
  }
}
