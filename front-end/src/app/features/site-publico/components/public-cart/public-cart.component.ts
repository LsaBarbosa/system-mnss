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
      <button class="cart-summary" (click)="toggleExpand()" [attr.aria-expanded]="expanded" aria-label="Expandir resumo do carrinho">
        <div class="info">
          <span class="count" aria-label="Número de itens">{{ itemCount$ | async }}</span>
          <span class="label">itens no carrinho</span>
        </div>
        <div class="total">
          {{ subtotal$ | async | currency: 'BRL' }}
          <span class="expand-icon" aria-hidden="true">{{ expanded ? '↓' : '↑' }}</span>
        </div>
      </button>

      <div class="cart-details" *ngIf="expanded" role="region" aria-label="Detalhes do carrinho">
        <div class="items-list">
          <div class="cart-item" *ngFor="let item of items$ | async">
            <div class="item-info">
              <span class="qnt">{{ item.quantity }}x</span>
              <span class="name">{{ item.name }}</span>
            </div>
            <div class="item-actions">
              <span class="price">{{ item.price * item.quantity | currency: 'BRL' }}</span>
              <button (click)="removeItem(item)" class="remove-btn" [attr.aria-label]="'Remover ' + item.name">×</button>
            </div>
          </div>
        </div>
        <a routerLink="/checkout" class="checkout-btn">Finalizar Pedido</a>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        --color-cream: #fff7e8;
        --color-gold: #f5a623;
        --color-brown: #6b3f1d;
        --color-brown-dark: #3d2b1f;
        --color-brown-light: #9c6b3f;
        --color-orange: #e67e22;
        --color-white: #ffffff;
        --color-whatsapp: #1e7e34;
        --color-danger: #c0392b;
        --shadow-soft: 0 12px 32px rgba(61, 43, 31, 0.1);
      }

      /* CARDAPIO-06.01 — Restyle cart floating element */
      .cart-container {
        position: fixed;
        bottom: 24px;
        left: 50%;
        right: auto;
        transform: translateX(-50%);
        width: 90%;
        max-width: 500px;
        background: var(--color-brown);
        color: var(--color-white);
        border-radius: 16px;
        box-shadow: 0 12px 32px rgba(61, 43, 31, 0.16);
        z-index: 1000;
        overflow: hidden;
        animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }

      @keyframes slideUp {
        from {
          transform: translate(-50%, 100%);
          opacity: 0;
        }
        to {
          transform: translate(-50%, 0);
          opacity: 1;
        }
      }

      /* CARDAPIO-06.03 — Cart accessibility improvements */
      .cart-summary {
        padding: 16px 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border: none;
        background-color: var(--color-brown);
        color: var(--color-white);
        cursor: pointer;
        width: 100%;
        transition: background-color 0.2s;
        text-align: left;
        font-size: 1rem;
      }

      .cart-summary:hover {
        background-color: var(--color-brown-dark);
      }

      .cart-summary:focus-visible {
        outline: 3px solid var(--color-gold);
        outline-offset: -3px;
      }

      .info {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .count {
        background-color: var(--color-gold);
        color: var(--color-brown-dark);
        width: 32px;
        height: 32px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        font-size: 0.9rem;
      }

      .label {
        font-weight: 600;
        font-size: 0.95rem;
      }

      .total {
        font-weight: 700;
        font-size: 1rem;
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .expand-icon {
        font-size: 0.8rem;
        opacity: 0.7;
      }

      .cart-details {
        background-color: var(--color-white);
        color: var(--color-brown-dark);
        padding: 16px;
        border-top: 1px solid rgba(156, 107, 63, 0.12);
      }

      .items-list {
        max-height: 240px;
        overflow-y: auto;
        margin-bottom: 16px;
      }

      .items-list::-webkit-scrollbar {
        width: 6px;
      }

      .items-list::-webkit-scrollbar-track {
        background: rgba(245, 166, 35, 0.1);
        border-radius: 3px;
      }

      .items-list::-webkit-scrollbar-thumb {
        background: var(--color-gold);
        border-radius: 3px;
      }

      .cart-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid rgba(156, 107, 63, 0.12);
      }

      .item-info {
        display: flex;
        gap: 8px;
        align-items: center;
        flex: 1;
      }

      .qnt {
        font-weight: 700;
        color: var(--color-gold);
        min-width: 28px;
      }

      .name {
        font-weight: 500;
        color: var(--color-brown);
        font-size: 0.95rem;
      }

      .item-actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }

      .price {
        font-weight: 600;
        font-size: 0.9rem;
        color: var(--color-brown-dark);
        min-width: 60px;
        text-align: right;
      }

      .remove-btn {
        border: none;
        background: none;
        color: var(--color-danger);
        font-size: 1.4rem;
        cursor: pointer;
        padding: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        border-radius: 6px;
        transition: all 0.2s;
      }

      .remove-btn:hover {
        background-color: rgba(192, 57, 43, 0.12);
      }

      .remove-btn:focus-visible {
        outline: 3px solid var(--color-gold);
        outline-offset: 2px;
      }

      .checkout-btn {
        display: block;
        width: 100%;
        padding: 14px;
        background-color: var(--color-whatsapp);
        color: var(--color-white);
        text-align: center;
        text-decoration: none;
        border: none;
        border-radius: 8px;
        font-weight: 700;
        font-size: 1rem;
        cursor: pointer;
        transition: all 0.2s;
        box-shadow: 0 4px 12px rgba(30, 126, 52, 0.2);
      }

      .checkout-btn:hover {
        background-color: #16662a;
        transform: scale(1.02);
      }

      .checkout-btn:focus-visible {
        outline: 3px solid var(--color-gold);
        outline-offset: 2px;
      }

      /* CARDAPIO-06.02 — Improve cart on mobile */
      @media (max-width: 768px) {
        .cart-container {
          bottom: 16px;
          width: calc(100% - 32px);
          max-width: none;
          border-radius: 12px;
        }

        .cart-summary {
          padding: 12px 16px;
        }

        .count {
          width: 28px;
          height: 28px;
          font-size: 0.85rem;
        }

        .label {
          font-size: 0.85rem;
        }

        .total {
          font-size: 0.95rem;
        }

        .cart-details {
          padding: 12px;
        }

        .items-list {
          max-height: 180px;
          margin-bottom: 12px;
        }

        .cart-item {
          padding: 8px 0;
        }

        .item-info {
          gap: 6px;
        }

        .item-actions {
          gap: 8px;
        }

        .remove-btn {
          width: 28px;
          height: 28px;
          font-size: 1.2rem;
        }

        .checkout-btn {
          padding: 12px;
          font-size: 0.95rem;
        }
      }
    `
  ]
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
