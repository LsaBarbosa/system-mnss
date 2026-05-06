import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { PublicMenuService } from '../../data-access/public-menu.service';
import { PublicMenuResponse } from '../../domain/public-menu.model';
import { PublicProductCardComponent } from '../../components/public-product-card/public-product-card.component';
import { PublicCartComponent } from '../../components/public-cart/public-cart.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-public-menu',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    PublicProductCardComponent,
    PublicCartComponent,
    RouterLink
  ],
  template: `
    <header class="public-header">
      <div class="brand">Cardápio Online</div>
      <div class="spacer"></div>
      <nav class="nav-links">
        <a routerLink="/" class="nav-link nav-icon" title="Início">
          🏠
        </a>
      </nav>
    </header>

    <div class="menu-container">
      <div class="search-section">
        <div class="search-field">
          <span class="search-icon">🔍</span>
          <input type="text" [(ngModel)]="searchQuery" (ngModelChange)="onSearchChange($event)" placeholder="Ex: Pão francês">
          <button class="clear-btn" *ngIf="searchQuery" (click)="clearSearch()" title="Limpar busca">
            ✖
          </button>
        </div>
      </div>

      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <p>Carregando cardápio...</p>
      </div>

      <div class="empty-state" *ngIf="!loading && menu.length === 0">
        <div class="icon">🍽️</div>
        <p>Nenhum produto encontrado.</p>
      </div>

      <div class="category-section" *ngFor="let categoryMenu of menu">
        <h2 class="category-title">{{ categoryMenu.category.name }}</h2>
        <p class="category-description" *ngIf="categoryMenu.category.description">
          {{ categoryMenu.category.description }}
        </p>

        <div class="products-grid">
          <app-public-product-card 
            *ngFor="let product of categoryMenu.products" 
            [product]="product">
          </app-public-product-card>
        </div>
      </div>
      <!-- Cart Floating Indicator -->
      <app-public-cart></app-public-cart>
    </div>
  `,
  styles: [`
    .public-header {
      display: flex;
      align-items: center;
      padding: 16px 24px;
      background-color: #1e3c72;
      color: white;
    }

    .brand {
      font-size: 1.25rem;
      font-weight: 700;
    }

    .spacer {
      flex: 1 1 auto;
    }

    .nav-links {
      display: flex;
      gap: 16px;
      align-items: center;
    }

    .nav-link {
      color: white;
      text-decoration: none;
      font-weight: 500;
      padding: 8px 16px;
      border-radius: 4px;
      transition: background-color 0.2s;
    }

    .nav-link:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }

    .nav-icon {
      font-size: 1.5rem;
      padding: 4px 8px;
    }

    .menu-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
    }

    .search-section {
      margin-bottom: 32px;
      max-width: 600px;
      margin-left: auto;
      margin-right: auto;
    }

    .search-field {
      display: flex;
      align-items: center;
      border: 1px solid #ccc;
      border-radius: 8px;
      padding: 8px 16px;
      background-color: white;
    }

    .search-icon {
      margin-right: 8px;
      color: #7f8c8d;
    }

    .search-field input {
      flex: 1;
      border: none;
      outline: none;
      font-size: 1rem;
      padding: 8px 0;
    }

    .clear-btn {
      background: none;
      border: none;
      color: #7f8c8d;
      cursor: pointer;
      font-size: 1rem;
      padding: 4px;
    }

    .clear-btn:hover {
      color: #e74c3c;
    }

    .category-section {
      margin-bottom: 48px;
    }

    .category-title {
      font-size: 2rem;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 8px;
      padding-bottom: 8px;
      border-bottom: 2px solid #eee;
    }

    .category-description {
      color: #7f8c8d;
      margin-bottom: 24px;
    }

    .products-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 24px;
    }

    .loading-state, .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 24px;
      color: #95a5a6;
    }

    .empty-state .icon {
      font-size: 48px;
      margin-bottom: 16px;
    }

    .spinner {
      display: inline-block;
      width: 40px;
      height: 40px;
      border: 4px solid rgba(0,0,0,0.1);
      border-radius: 50%;
      border-top-color: #1e3c72;
      animation: spin 1s ease-in-out infinite;
      margin-bottom: 16px;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    @media (max-width: 600px) {
      .menu-container {
        padding: 16px;
      }

      .category-title {
        font-size: 1.5rem;
      }

      .products-grid {
        grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        gap: 16px;
      }
    }
  `]
})
export class PublicMenuComponent implements OnInit {
  menu: PublicMenuResponse[] = [];
  loading = false;
  searchQuery = '';
  private searchSubject = new Subject<string>();

  constructor(private readonly menuService: PublicMenuService) {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(search => {
      this.loadMenu(search);
    });
  }

  ngOnInit(): void {
    this.loadMenu();
  }

  onSearchChange(search: string): void {
    this.searchSubject.next(search);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchSubject.next('');
  }

  private loadMenu(search?: string): void {
    this.loading = true;
    this.menuService.getMenu(search).subscribe({
      next: (data) => {
        this.menu = data;
        this.loading = false;
      },
      error: () => {
        this.menu = [];
        this.loading = false;
      }
    });
  }
}
