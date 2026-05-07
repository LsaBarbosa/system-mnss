import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartService, CartItem } from '../../data-access/cart.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-public-cart',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="cart-container" *ngIf="(itemCount$ | async) || 0 > 0">
      <div class="cart-summary" (click)="toggleExpand()">
        <div class="info">
          <span class="count">{{ itemCount$ | async }}</span>
          <span class="label">itens no carrinho</span>
        </div>
        <div class="total">
          {{ subtotal$ | async | currency:'BRL' }}
          <span class="expand-icon">{{ expanded ? '↓' : '↑' }}</span>
        </div>
      </div>

      <div class="cart-details" *ngIf="expanded">
        <div class="items-list">
          <div class="cart-item" *ngFor="let item of items$ | async">
            <div class="item-info">
              <span class="qnt">{{ item.quantity }}x</span>
              <span class="name">{{ item.name }}</span>
            </div>
            <div class="item-actions">
              <span class="price">{{ (item.price * item.quantity) | currency:'BRL' }}</span>
              <button (click)="removeItem(item)" class="remove-btn">×</button>
            </div>
          </div>
        </div>
        <a routerLink="/checkout" class="checkout-btn">Finalizar Pedido</a>
      </div>
    </div>
  `,
  styles: [`
    .cart-container {
      position: fixed;
      bottom: 24px;
      left: 50%;
      transform: translateX(-50%);
      width: 90%;
      max-width: 500px;
      background: #2c3e50;
      color: white;
      border-radius: 16px;
      box-shadow: 0 10px 40px rgba(0,0,0,0.3);
      z-index: 1000;
      overflow: hidden;
      animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    @keyframes slideUp {
      from { transform: translate(-50%, 100%); opacity: 0; }
      to { transform: translate(-50%, 0); opacity: 1; }
    }

    .cart-summary {
      padding: 16px 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      cursor: pointer;
      background: linear-gradient(135deg, #2c3e50 0%, #1a252f 100%);

      .info {
        display: flex;
        align-items: center;
        gap: 12px;

        .count {
          background: #3498db;
          color: white;
          width: 28px;
          height: 28px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-weight: 700;
          font-size: 0.9rem;
        }

        .label {
          font-weight: 600;
        }
      }

      .total {
        font-weight: 700;
        font-size: 1.1rem;
        display: flex;
        align-items: center;
        gap: 12px;

        .expand-icon {
          font-size: 0.8rem;
          opacity: 0.5;
        }
      }
    }

    .cart-details {
      background: white;
      color: #2c3e50;
      padding: 16px;
      border-top: 1px solid #eee;
    }

    .items-list {
      max-height: 200px;
      overflow-y: auto;
      margin-bottom: 16px;
    }

    .cart-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px solid #f5f5f5;

      .item-info {
        display: flex;
        gap: 8px;
        .qnt { font-weight: 700; color: #3498db; }
        .name { font-weight: 500; }
      }

      .item-actions {
        display: flex;
        align-items: center;
        gap: 12px;
        .price { font-weight: 600; font-size: 0.9rem; }
        .remove-btn {
          border: none;
          background: none;
          color: #e74c3c;
          font-size: 1.2rem;
          cursor: pointer;
          padding: 0 4px;
        }
      }
    }

    .checkout-btn {
      display: block;
      width: 100%;
      padding: 14px;
      background: #2ecc71;
      color: white;
      text-align: center;
      text-decoration: none;
      border-radius: 10px;
      font-weight: 700;
      transition: background 0.2s;

      &:hover {
        background: #27ae60;
      }
    }
  `]
})
export class PublicCartComponent {
  items$: Observable<CartItem[]>;
  itemCount$: Observable<number>;
  subtotal$: Observable<number>;
  expanded = false;

  constructor(private cartService: CartService) {
    this.items$ = this.cartService.items$;
    this.itemCount$ = this.cartService.getItemCount();
    this.subtotal$ = this.cartService.getSubtotal();
  }

  toggleExpand() {
    this.expanded = !this.expanded;
  }

  removeItem(item: CartItem) {
    this.cartService.removeItem(item.productId, item.observation);
  }
}
