import { Component, Input } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { PublicProductResponse } from '../../domain/public-menu.model';
import { CartService } from '../../data-access/cart.service';

@Component({
  selector: 'app-public-product-card',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  template: `
    <div class="product-card">
      <img
        *ngIf="product.imageUrl"
        class="product-image"
        [src]="product.imageUrl"
        [alt]="product.name"
        loading="lazy"
        decoding="async"
      />
      <div *ngIf="!product.imageUrl" class="product-placeholder" aria-hidden="true">
        <span class="icon">🍽️</span>
      </div>
      <div class="out-of-stock-badge" *ngIf="!product.available" role="status" aria-label="Produto esgotado">
        ESGOTADO
      </div>
      <div class="product-header">
        <h3 class="product-title">{{ product.name }}</h3>
      </div>
      <div class="product-content">
        <p class="product-description" *ngIf="product.description">{{ product.description }}</p>
        <div class="product-prices" aria-label="Informações de preço">
          <span class="price" [class.has-promotion]="product.promotionalPrice">
            {{ product.price | currency: 'BRL' }}
          </span>
          <span class="promotional-price" *ngIf="product.promotionalPrice" aria-label="Preço promocional">
            {{ product.promotionalPrice | currency: 'BRL' }}
          </span>
        </div>
        <button
          class="add-button"
          [disabled]="!product.available"
          (click)="addToCart()"
          [attr.aria-label]="product.available ? 'Adicionar ' + product.name + ' ao carrinho' : 'Produto ' + product.name + ' indisponível'"
        >
          {{ product.available ? 'Adicionar' : 'Indisponível' }}
        </button>
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
        --color-danger: #c0392b;
      }

      .product-card {
        height: 100%;
        display: flex;
        flex-direction: column;
        border-radius: 16px;
        overflow: hidden;
        background-color: var(--color-white);
        box-shadow: 0 4px 16px rgba(61, 43, 31, 0.08);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        border: 1px solid rgba(156, 107, 63, 0.12);
        position: relative;
      }

      .product-card:hover {
        transform: translateY(-8px);
        box-shadow: 0 12px 32px rgba(61, 43, 31, 0.14);
        border-color: var(--color-gold);
      }

      .product-image {
        width: 100%;
        height: 180px;
        object-fit: cover;
      }

      .product-placeholder {
        height: 180px;
        background: linear-gradient(135deg, var(--color-cream) 0%, rgba(245, 166, 35, 0.1) 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--color-brown-light);
      }

      .product-placeholder .icon {
        font-size: 48px;
        filter: drop-shadow(0 2px 4px rgba(61, 43, 31, 0.1));
      }

      .product-header {
        padding: 16px 16px 8px;
      }

      .product-title {
        font-size: 1.05rem;
        font-weight: 700;
        line-height: 1.3;
        margin: 0;
        color: var(--color-brown-dark);
      }

      .product-content {
        padding: 0 16px 16px;
        display: flex;
        flex-direction: column;
        flex: 1;
        justify-content: space-between;
      }

      .product-description {
        color: var(--color-brown);
        font-size: 0.9rem;
        margin-bottom: 12px;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .product-prices {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-bottom: 12px;
      }

      .price {
        font-weight: 700;
        font-size: 1rem;
        color: var(--color-brown-dark);
      }

      .price.has-promotion {
        text-decoration: line-through;
        color: var(--color-brown-light);
        font-size: 0.9rem;
        font-weight: 600;
      }

      .promotional-price {
        font-weight: 800;
        font-size: 1.25rem;
        color: var(--color-orange);
      }

      .add-button {
        width: 100%;
        padding: 12px;
        border-radius: 8px;
        border: none;
        background-color: var(--color-gold);
        color: var(--color-brown-dark);
        font-weight: 700;
        font-size: 0.95rem;
        cursor: pointer;
        transition: all 0.2s ease;
        box-shadow: 0 4px 12px rgba(245, 166, 35, 0.2);
      }

      .add-button:hover:not(:disabled) {
        background-color: var(--color-orange);
        color: var(--color-white);
        transform: scale(1.02);
        box-shadow: 0 6px 16px rgba(230, 126, 34, 0.3);
      }

      .add-button:active:not(:disabled) {
        transform: scale(0.98);
      }

      .add-button:focus-visible {
        outline: 3px solid var(--color-brown);
        outline-offset: 2px;
      }

      .add-button:disabled {
        background-color: var(--color-brown-light);
        color: var(--color-white);
        box-shadow: none;
        cursor: not-allowed;
        opacity: 0.7;
      }

      .out-of-stock-badge {
        position: absolute;
        top: 8px;
        right: 8px;
        background-color: var(--color-danger);
        color: var(--color-white);
        padding: 4px 12px;
        font-size: 0.7rem;
        font-weight: 800;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(192, 57, 43, 0.2);
        z-index: 1;
        letter-spacing: 0.05em;
      }

      @media (max-width: 768px) {
        .product-card {
          border-radius: 12px;
        }

        .product-image {
          height: 140px;
        }

        .product-placeholder {
          height: 140px;
        }

        .product-title {
          font-size: 1rem;
        }

        .add-button {
          padding: 10px;
          font-size: 0.9rem;
        }
      }
    `
  ]
})
export class PublicProductCardComponent {
  @Input({ required: true }) product!: PublicProductResponse;

  constructor(private cartService: CartService) {}

  addToCart() {
    this.cartService.addItem({
      productId: this.product.id,
      name: this.product.name,
      price: this.product.promotionalPrice || this.product.price,
      quantity: 1,
      imageUrl: this.product.imageUrl
    });
  }
}
