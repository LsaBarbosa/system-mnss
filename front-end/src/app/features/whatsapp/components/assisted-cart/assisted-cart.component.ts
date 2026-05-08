import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WhatsAppService } from '../../data-access/whatsapp.service';
import { ErrorMessageService } from '../../../../core/errors/error-message.service';
import {
  ConversationResponse,
  WhatsAppOrderItemRequest,
  CreateWhatsAppOrderRequest
} from '../../domain/whatsapp.model';
import { PublicProductResponse } from '../../../site-publico/domain/public-menu.model';
import { DeliveryType } from '../../../../core/models/order.model';

@Component({
  selector: 'app-assisted-cart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="cart-workspace">
      <div class="cart-header">
        <h2>Novo Pedido Assistido</h2>
        <p>Montando pedido para {{ conversation.customerName || conversation.customerPhone }}</p>
      </div>

      <div class="cart-layout">
        <section class="catalog-panel">
          <div class="search-box">
            <input type="text" placeholder="Buscar produto..." [(ngModel)]="searchTerm" />
          </div>
          <div class="product-grid">
            <div *ngFor="let prod of filteredCatalog()" class="product-card" (click)="addItem(prod)">
              <div class="prod-info">
                <strong>{{ prod.name }}</strong>
                <span>{{ prod.price | currency: 'BRL' }}</span>
              </div>
              <button class="add-btn">+</button>
            </div>
          </div>
        </section>

        <section class="summary-panel">
          <h3>Itens no Carrinho</h3>
          <div class="items-scroll">
            <div *ngFor="let item of items; let i = index" class="cart-item">
              <div class="item-details">
                <strong>{{ getProductName(item.productId) }}</strong>
                <div class="item-calc">
                  <input type="number" [(ngModel)]="item.quantity" min="1" class="qty-field" />
                  x {{ getProductPrice(item.productId) | currency: 'BRL' }} =
                  {{ (item.quantity || 1) * getProductPrice(item.productId) | currency: 'BRL' }}
                </div>
              </div>
              <button class="remove-btn" (click)="removeItem(i)">×</button>
            </div>
            <div *ngIf="items.length === 0" class="empty-cart-msg">Carrinho vazio</div>
          </div>

          <div class="order-forms">
            <div class="form-group">
              <label>Tipo de entrega</label>
              <select [(ngModel)]="deliveryType">
                <option value="PICKUP">Retirada na loja</option>
                <option value="DELIVERY">Entrega a domicílio</option>
              </select>
            </div>
            <div class="form-group">
              <label>Observações</label>
              <textarea [(ngModel)]="notes" placeholder="Ex: Sem cebola, troco para R$ 50..."></textarea>
            </div>
          </div>

          <div class="cart-footer">
            <div class="total-line">
              <span>Total do Pedido</span>
              <strong>{{ calculateTotal() | currency: 'BRL' }}</strong>
            </div>
            <button class="finish-btn" [disabled]="items.length === 0 || isSubmitting" (click)="createOrder()">
              {{ isSubmitting ? 'Finalizando...' : 'Confirmar e Enviar' }}
            </button>
          </div>
        </section>
      </div>
    </div>
  `,
  styles: [
    `
      .cart-workspace {
        display: flex;
        flex-direction: column;
        height: 100%;
        background: #ffffff;
      }

      .cart-header {
        padding: 24px;
        border-bottom: 1px solid #e1e8eb;
        h2 {
          margin: 0;
          font-size: 1.3rem;
          color: #172126;
        }
        p {
          margin: 5px 0 0;
          color: #5a6970;
          font-size: 0.9rem;
        }
      }

      .cart-layout {
        flex: 1;
        display: grid;
        grid-template-columns: 1fr 380px;
        overflow: hidden;
      }

      .catalog-panel {
        padding: 24px;
        overflow-y: auto;
        background: #fcfdfe;
        border-right: 1px solid #e1e8eb;
      }

      .search-box {
        margin-bottom: 20px;
        input {
          width: 100%;
          padding: 12px 16px;
          border: 1px solid #d4dde1;
          border-radius: 8px;
          outline: none;
          &:focus {
            border-color: #f1b74a;
          }
        }
      }

      .product-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 12px;
      }

      .product-card {
        padding: 16px;
        border: 1px solid #e1e8eb;
        border-radius: 10px;
        background: #ffffff;
        display: flex;
        justify-content: space-between;
        align-items: center;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: #f1b74a;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        }
      }

      .prod-info {
        display: flex;
        flex-direction: column;
        strong {
          font-size: 0.95rem;
          color: #233239;
        }
        span {
          font-size: 0.85rem;
          color: #1d8f62;
          font-weight: 700;
          margin-top: 4px;
        }
      }

      .add-btn {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        border: none;
        background: #eef2f4;
        color: #172126;
        font-weight: 800;
        cursor: pointer;
      }

      .summary-panel {
        padding: 24px;
        display: flex;
        flex-direction: column;
        background: #ffffff;

        h3 {
          margin: 0 0 16px;
          font-size: 1.1rem;
        }
      }

      .items-scroll {
        flex: 1;
        overflow-y: auto;
        margin-bottom: 20px;
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      .cart-item {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding-bottom: 10px;
        border-bottom: 1px solid #f0f3f5;
      }

      .item-calc {
        margin-top: 4px;
        font-size: 0.85rem;
        color: #5a6970;
      }

      .qty-field {
        width: 44px;
        padding: 2px 4px;
        border: 1px solid #d4dde1;
        border-radius: 4px;
        text-align: center;
      }

      .remove-btn {
        background: none;
        border: none;
        color: #b54040;
        font-size: 1.2rem;
        cursor: pointer;
        padding: 4px;
      }

      .order-forms {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-bottom: 20px;
        padding-top: 16px;
        border-top: 1px solid #f0f3f5;
      }

      .form-group {
        display: flex;
        flex-direction: column;
        gap: 4px;
        label {
          font-size: 0.85rem;
          font-weight: 600;
          color: #5a6970;
        }
        select,
        textarea {
          padding: 10px;
          border: 1px solid #d4dde1;
          border-radius: 6px;
          font-family: inherit;
          &:focus {
            border-color: #f1b74a;
            outline: none;
          }
        }
        textarea {
          height: 60px;
          resize: none;
        }
      }

      .cart-footer {
        padding-top: 16px;
        border-top: 2px solid #e1e8eb;
      }

      .total-line {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        span {
          color: #5a6970;
        }
        strong {
          font-size: 1.4rem;
          color: #172126;
        }
      }

      .finish-btn {
        width: 100%;
        padding: 14px;
        background: #1d8f62;
        color: #ffffff;
        border: none;
        border-radius: 8px;
        font-weight: 700;
        font-size: 1rem;
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

      .empty-cart-msg {
        padding: 40px 20px;
        text-align: center;
        color: #8fa0a8;
        font-style: italic;
      }
    `
  ]
})
export class AssistedCartComponent implements OnInit {
  @Input() conversation!: ConversationResponse;
  @Output() orderCreated = new EventEmitter<void>();

  catalog: PublicProductResponse[] = [];
  items: WhatsAppOrderItemRequest[] = [];
  deliveryType: DeliveryType = 'PICKUP';
  notes = '';
  searchTerm = '';
  isSubmitting = false;

  constructor(
    private whatsappService: WhatsAppService,
    private errorMessageService: ErrorMessageService
  ) {}

  ngOnInit(): void {
    this.whatsappService.getCatalog().subscribe((data) => (this.catalog = data));
  }

  filteredCatalog(): PublicProductResponse[] {
    if (!this.searchTerm.trim()) return this.catalog;
    return this.catalog.filter((p) => p.name.toLowerCase().includes(this.searchTerm.toLowerCase()));
  }

  addItem(product: PublicProductResponse): void {
    const existing = this.items.find((i) => i.productId === product.id);
    if (existing) {
      existing.quantity = (existing.quantity || 1) + 1;
    } else {
      this.items.push({
        productId: product.id,
        quantity: 1
      });
    }
  }

  removeItem(index: number): void {
    this.items.splice(index, 1);
  }

  getProductName(id: string): string {
    return this.catalog.find((p) => p.id === id)?.name || 'Produto Desconhecido';
  }

  getProductPrice(id: string): number {
    return this.catalog.find((p) => p.id === id)?.price || 0;
  }

  calculateTotal(): number {
    return this.items.reduce((acc, item) => {
      const price = this.getProductPrice(item.productId);
      return acc + (item.quantity || 1) * price;
    }, 0);
  }

  createOrder(): void {
    if (this.items.length === 0) return;

    this.isSubmitting = true;
    const request: CreateWhatsAppOrderRequest = {
      conversationId: this.conversation.id,
      customerPhone: this.conversation.customerPhone,
      customerName: this.conversation.customerName || this.conversation.customerPhone,
      deliveryType: this.deliveryType,
      items: this.items,
      notes: this.notes
    };

    this.whatsappService.createOrder(request).subscribe({
      next: () => {
        this.errorMessageService.showMessage('Pedido criado com sucesso!');
        this.orderCreated.emit();
        this.isSubmitting = false;
      },
      error: (err) => {
        this.errorMessageService.showMessage('Erro ao criar pedido: ' + (err.error?.message || err.message));
        this.isSubmitting = false;
      }
    });
  }
}
