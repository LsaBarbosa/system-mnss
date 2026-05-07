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
      <img *ngIf="product.imageUrl" class="product-image" [src]="product.imageUrl" [alt]="product.name">
      <div *ngIf="!product.imageUrl" class="product-placeholder">
        <span class="icon">🍽️</span>
      </div>
      <div class="out-of-stock-badge" *ngIf="!product.available">
        ESGOTADO
      </div>
      <div class="product-header">
        <h3 class="product-title">{{ product.name }}</h3>
      </div>
      <div class="product-content">
        <p class="product-description" *ngIf="product.description">{{ product.description }}</p>
        <div class="product-prices">
          <span class="price" [class.has-promotion]="product.promotionalPrice">
            {{ product.price | currency:'BRL' }}
          </span>
          <span class="promotional-price" *ngIf="product.promotionalPrice">
            {{ product.promotionalPrice | currency:'BRL' }}
          </span>
        </div>
        <button class="add-button" [disabled]="!product.available" (click)="addToCart()">
          {{ product.available ? 'Adicionar' : 'Indisponível' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .product-card {
      height: 100%;
      display: flex;
      flex-direction: column;
      border-radius: 16px;
      overflow: hidden;
      background-color: white;
      box-shadow: 0 4px 20px rgba(0,0,0,0.06);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid rgba(0,0,0,0.05);
      position: relative;
      
      &:hover {
        transform: translateY(-8px);
        box-shadow: 0 12px 30px rgba(0,0,0,0.12);
        border-color: #3498db;
      }
    }

    .product-image {
      width: 100%;
      height: 180px;
      object-fit: cover;
    }

    .product-placeholder {
      height: 180px;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #95a5a6;

      .icon {
        font-size: 48px;
        filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
      }
    }

    .product-header {
      padding: 20px 20px 10px;
    }

    .product-title {
      font-size: 1.15rem;
      font-weight: 700;
      line-height: 1.3;
      margin: 0;
      color: #2c3e50;
    }

    .product-content {
      padding: 0 20px 20px;
      display: flex;
      flex-direction: column;
      flex: 1;
      justify-content: space-between;
    }

    .product-description {
      color: #7f8c8d;
      font-size: 0.95rem;
      margin-bottom: 20px;
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
      margin-bottom: 15px;
    }

    .price {
      font-weight: 600;
      font-size: 1.1rem;
      color: #34495e;

      &.has-promotion {
        text-decoration: line-through;
        color: #bdc3c7;
        font-size: 0.9rem;
      }
    }

    .promotional-price {
      font-weight: 800;
      font-size: 1.3rem;
      color: #e74c3c;
    }

    .add-button {
      width: 100%;
      padding: 12px;
      border-radius: 10px;
      border: none;
      background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
      color: white;
      font-weight: 700;
      font-size: 1rem;
      cursor: pointer;
      transition: all 0.2s ease;
      box-shadow: 0 4px 10px rgba(52, 152, 219, 0.3);

      &:hover {
        background: linear-gradient(135deg, #2980b9 0%, #2471a3 100%);
        transform: scale(1.02);
        box-shadow: 0 6px 15px rgba(52, 152, 219, 0.4);
      }

      &:active {
        transform: scale(0.98);
      }

      &:disabled {
        background: #bdc3c7;
        box-shadow: none;
        cursor: not-allowed;
        transform: none;
      }
    }

    .out-of-stock-badge {
      position: absolute;
      top: 15px;
      right: -30px;
      background-color: #e74c3c;
      color: white;
      padding: 5px 40px;
      font-size: 0.75rem;
      font-weight: 800;
      transform: rotate(45deg);
      box-shadow: 0 2px 4px rgba(0,0,0,0.2);
      z-index: 1;
    }
  `]
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
