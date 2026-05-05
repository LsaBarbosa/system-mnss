import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WhatsAppService } from '../../data-access/whatsapp.service';
import { ConversationResponse, MessageResponse } from '../../models/whatsapp.model';
import { ConversationListComponent } from '../../components/conversation-list/conversation-list.component';
import { ChatViewComponent } from '../../components/chat-view/chat-view.component';
import { AssistedCartComponent } from '../../components/assisted-cart/assisted-cart.component';
import { Subscription, interval, startWith, switchMap } from 'rxjs';

@Component({
  selector: 'app-whatsapp-panel',
  standalone: true,
  imports: [
    CommonModule,
    ConversationListComponent,
    ChatViewComponent,
    AssistedCartComponent
  ],
  templateUrl: './whatsapp-panel.component.html',
  styleUrl: './whatsapp-panel.component.scss'
})
export class WhatsAppPanelComponent implements OnInit, OnDestroy {
  conversations: ConversationResponse[] = [];
  selectedConversation?: ConversationResponse;
  messages: MessageResponse[] = [];
  isLoading = false;
  showCart = false;
  
  private pollingSubscription?: Subscription;

  constructor(private whatsappService: WhatsAppService) {}

  ngOnInit(): void {
    this.loadConversations();
    
    // Auto-refresh conversations every 10 seconds
    this.pollingSubscription = interval(10000)
      .pipe(startWith(0), switchMap(() => this.whatsappService.getConversations()))
      .subscribe(data => this.conversations = data);
  }

  ngOnDestroy(): void {
    this.pollingSubscription?.unsubscribe();
  }

  loadConversations(): void {
    this.isLoading = true;
    this.whatsappService.getConversations().subscribe({
      next: (data) => {
        this.conversations = data;
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  selectConversation(conversation: ConversationResponse): void {
    this.selectedConversation = conversation;
    this.loadMessages(conversation.id);
  }

  loadMessages(conversationId: string): void {
    this.whatsappService.getMessages(conversationId).subscribe(data => {
      this.messages = data;
    });
  }

  onSendMessage(content: string): void {
    if (!this.selectedConversation) return;
    
    this.whatsappService.sendMessage(this.selectedConversation.id, content).subscribe(() => {
      this.loadMessages(this.selectedConversation!.id);
    });
  }

  toggleCart(): void {
    this.showCart = !this.showCart;
  }
}
