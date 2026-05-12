import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { Title, Meta } from '@angular/platform-browser';
import { PublicMenuService } from '../../data-access/public-menu.service';
import { PublicMenuResponse } from '../../domain/public-menu.model';
import { PublicProductCardComponent } from '../../components/public-product-card/public-product-card.component';
import { PublicCartComponent } from '../../components/public-cart/public-cart.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-public-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, PublicProductCardComponent, PublicCartComponent, RouterLink],
  template: `
    <header class="public-menu-header" id="topo-cardapio">
      <a
        routerLink="/"
        class="menu-brand-logo"
        aria-label="Voltar para a home da Padaria Nova Aliança"
      >
        <img
          src="assets/images/site/logo.png"
          alt="Padaria e Lanchonete Nova Aliança"
          decoding="async"
        />
      </a>

      <div class="spacer"></div>

      <nav class="menu-nav" aria-label="Navegação do cardápio">
        <a routerLink="/" class="menu-nav-link">Início</a>
      </nav>
    </header>

    <section class="menu-hero">
      <span class="section-eyebrow">Cardápio Online</span>
      <h1>Cardápio Online Nova Aliança</h1>
      <p>Escolha seus produtos, adicione ao carrinho e finalize seu pedido de forma simples.</p>
    </section>

    <p class="availability-note">
      Produtos e disponibilidade podem variar conforme o horário e a produção do dia.
    </p>

    <div class="menu-container">
      <div class="search-section">
        <div class="search-field">
          <span class="search-icon" aria-hidden="true">🔍</span>

          <input
            type="text"
            [(ngModel)]="searchQuery"
            (ngModelChange)="onSearchChange($event)"
            placeholder="Buscar pão, café, bolo ou salgado"
            aria-label="Buscar produtos no cardápio"
          />

          <button
            class="clear-btn"
            *ngIf="searchQuery"
            (click)="clearSearch()"
            type="button"
            aria-label="Limpar busca"
          >
            ✖
          </button>
        </div>
      </div>

      <div class="category-chips" *ngIf="!loading && getAvailableCategories().length > 0">
        <button
          class="category-chip"
          [class.active]="selectedCategoryId === null"
          (click)="onCategoryChange(null)"
          type="button"
          aria-label="Filtrar todas as categorias"
        >
          Todos
        </button>
        <button
          class="category-chip"
          *ngFor="let categoryMenu of getAvailableCategories()"
          [class.active]="selectedCategoryId === categoryMenu.category.id"
          (click)="onCategoryChange(categoryMenu.category.id)"
          type="button"
          [attr.aria-label]="'Filtrar por ' + categoryMenu.category.name"
        >
          {{ categoryMenu.category.name }}
        </button>
      </div>

      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <p>Carregando produtos fresquinhos...</p>
      </div>

      <div class="empty-state" *ngIf="!loading && menu.length === 0">
        <div class="icon" aria-hidden="true">🔎</div>

        <p>Nenhum produto encontrado para sua busca.</p>
        <span>Tente procurar por pão, café, bolo ou salgado.</span>

        <button
          class="empty-clear-btn"
          *ngIf="searchQuery"
          type="button"
          (click)="clearSearch()"
        >
          Limpar busca
        </button>
      </div>

      <div class="category-section" *ngFor="let categoryMenu of menu" [attr.id]="'category-' + categoryMenu.category.id">
        <div class="category-header">
          <h2 class="category-title">{{ categoryMenu.category.name }}</h2>
          <p class="category-description" *ngIf="categoryMenu.category.description">
            {{ categoryMenu.category.description }}
          </p>
        </div>

        <div class="products-grid">
          <app-public-product-card *ngFor="let product of categoryMenu.products" [product]="product">
          </app-public-product-card>
        </div>
      </div>
      <!-- Cart Floating Indicator -->
      <app-public-cart></app-public-cart>
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
        --shadow-medium: 0 18px 42px rgba(61, 43, 31, 0.16);

        display: block;
        min-height: 100vh;
        background-color: var(--color-cream);
        color: var(--color-brown-dark);
      }

      /* CARDAPIO-01.02 — Header com logo */
      .public-menu-header {
        position: sticky;
        top: 0;
        z-index: 900;
        display: flex;
        align-items: center;
        gap: 16px;
        min-height: 72px;
        padding: 10px 24px;
        background-color: var(--color-brown);
        color: var(--color-white);
        box-shadow: 0 8px 24px rgba(61, 43, 31, 0.18);
      }

      .menu-brand-logo {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        text-decoration: none;
      }

      .menu-brand-logo img {
        display: block;
        height: 54px;
        width: auto;
        max-width: 180px;
        object-fit: contain;
      }

      .menu-brand-logo:focus-visible {
        outline: 3px solid var(--color-gold);
        outline-offset: 4px;
        border-radius: 8px;
      }

      .spacer {
        flex: 1 1 auto;
      }

      .menu-nav {
        display: flex;
        gap: 16px;
        align-items: center;
      }

      .menu-nav-link {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-height: 42px;
        padding: 8px 16px;
        border-radius: 999px;
        color: var(--color-white);
        text-decoration: none;
        font-weight: 800;
        transition: background-color 0.2s;
      }

      .menu-nav-link:hover {
        background-color: rgba(255, 247, 232, 0.14);
      }

      .menu-nav-link:focus-visible {
        outline: 3px solid var(--color-gold);
        outline-offset: 4px;
      }

      /* CARDAPIO-02.01 — Hero compacto */
      .menu-hero {
        max-width: 980px;
        margin: 0 auto;
        padding: 48px 24px 28px;
        text-align: center;
      }

      .section-eyebrow {
        display: inline-block;
        margin-bottom: 10px;
        color: var(--color-orange);
        font-weight: 900;
        font-size: 0.82rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
      }

      .menu-hero h1 {
        margin: 0 0 14px;
        color: var(--color-brown-dark);
        font-size: clamp(2rem, 5vw, 3rem);
        line-height: 1.1;
        font-weight: 900;
      }

      .menu-hero p {
        max-width: 680px;
        margin: 0 auto;
        color: var(--color-brown);
        font-size: 1.1rem;
        line-height: 1.65;
      }

      /* CARDAPIO-02.02 — Aviso de disponibilidade */
      .availability-note {
        max-width: 720px;
        margin: 14px auto 28px;
        padding: 10px 14px;
        border-radius: 999px;
        background-color: rgba(245, 166, 35, 0.14);
        color: var(--color-brown);
        text-align: center;
        font-weight: 700;
        font-size: 0.92rem;
      }

      .menu-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 24px;
      }

      /* CARDAPIO-03.01 — Busca como app */
      .search-section {
        max-width: 760px;
        margin: 0 auto 28px;
      }

      .search-field {
        min-height: 56px;
        display: flex;
        align-items: center;
        border: 1px solid rgba(156, 107, 63, 0.18);
        border-radius: 999px;
        padding: 8px 18px;
        background-color: var(--color-white);
        box-shadow: var(--shadow-soft);
      }

      .search-icon {
        margin-right: 10px;
        color: var(--color-brown-light);
      }

      .search-field input {
        flex: 1;
        min-width: 0;
        border: none;
        outline: none;
        font-size: 1rem;
        color: var(--color-brown-dark);
        background: transparent;
      }

      .search-field input::placeholder {
        color: var(--color-brown-light);
      }

      .clear-btn {
        width: 36px;
        height: 36px;
        border-radius: 999px;
        border: none;
        background-color: rgba(156, 107, 63, 0.12);
        color: var(--color-brown);
        cursor: pointer;
        font-weight: 900;
        transition: background-color 0.2s, color 0.2s;
        -webkit-tap-highlight-color: transparent;
      }

      .clear-btn:hover {
        background-color: rgba(230, 126, 34, 0.16);
        color: var(--color-orange);
      }

      .clear-btn:focus-visible {
        outline: 3px solid var(--color-gold);
        outline-offset: 2px;
      }

      .search-field:focus-within {
        border-color: var(--color-gold);
        box-shadow: 0 0 0 4px rgba(245, 166, 35, 0.16);
      }

      /* CARDAPIO-03.02 — Category chips navigation (improved) */
      .category-chips {
        max-width: 1200px;
        margin: 0 auto 36px;
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        padding: 0 24px;
        justify-content: center;
      }

      /* CARDAPIO-09.01 — Focus-visible on all elements */
      .category-chip {
        padding: 10px 18px;
        border-radius: 999px;
        border: 2px solid rgba(156, 107, 63, 0.2);
        background-color: var(--color-white);
        color: var(--color-brown);
        font-weight: 700;
        font-size: 0.9rem;
        cursor: pointer;
        transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        white-space: nowrap;
        box-shadow: 0 2px 8px rgba(61, 43, 31, 0.06);
        display: inline-flex;
        align-items: center;
        min-height: 40px;
      }

      .category-chip:hover:not(.active) {
        border-color: var(--color-gold);
        background-color: rgba(245, 166, 35, 0.12);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(61, 43, 31, 0.08);
      }

      .category-chip:focus-visible {
        outline: 3px solid var(--color-gold);
        outline-offset: 2px;
        border-radius: 999px;
      }

      .category-chip.active {
        background-color: var(--color-gold);
        border-color: var(--color-gold);
        color: var(--color-white);
        box-shadow: 0 6px 16px rgba(245, 166, 35, 0.28);
        font-weight: 800;
      }

      .category-chip.active:hover {
        background-color: var(--color-orange);
        border-color: var(--color-orange);
        box-shadow: 0 8px 20px rgba(230, 126, 34, 0.32);
        transform: translateY(-2px);
      }

      .category-chip.active:focus-visible {
        outline: 3px solid var(--color-brown-dark);
        outline-offset: 2px;
      }

      /* CARDAPIO-04.01 — Improved category headers */
      .category-section {
        max-width: 1200px;
        margin: 0 auto 56px;
        padding: 0 24px;
      }

      .category-header {
        margin-bottom: 28px;
      }

      .category-title {
        margin: 0 0 12px;
        color: var(--color-brown-dark);
        font-size: clamp(1.4rem, 4vw, 2rem);
        font-weight: 900;
        line-height: 1.15;
        letter-spacing: -0.01em;
      }

      .category-description {
        margin: 0;
        color: var(--color-brown);
        font-size: 0.95rem;
        line-height: 1.6;
        max-width: 600px;
      }

      /* CARDAPIO-04.02 — Responsive grid layout */
      .products-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 24px;
      }

      @media (max-width: 1024px) {
        .products-grid {
          grid-template-columns: repeat(2, 1fr);
          gap: 20px;
        }
      }

      /* CARDAPIO-08.01/08.02 — Improved loading and empty states */
      .loading-state,
      .empty-state {
        max-width: 420px;
        margin: 60px auto;
        padding: 48px 32px;
        border-radius: 20px;
        background-color: var(--color-white);
        color: var(--color-brown);
        text-align: center;
        box-shadow: var(--shadow-soft);
        border: 1px solid rgba(156, 107, 63, 0.08);
      }

      .loading-state p,
      .empty-state p {
        margin: 0;
        font-size: 1.05rem;
        font-weight: 600;
        color: var(--color-brown-dark);
      }

      .empty-state .icon {
        font-size: 56px;
        margin-bottom: 20px;
        display: block;
        line-height: 1;
      }

      .empty-state span {
        display: block;
        color: var(--color-brown-light);
        margin-top: 12px;
        font-size: 0.95rem;
        line-height: 1.5;
      }

      .empty-clear-btn {
        display: inline-flex;
        justify-content: center;
        align-items: center;
        min-height: 44px;
        padding: 10px 20px;
        margin-top: 20px;
        border-radius: 8px;
        background-color: var(--color-gold);
        color: var(--color-brown-dark);
        border: none;
        text-decoration: none;
        font-weight: 700;
        font-size: 0.95rem;
        cursor: pointer;
        transition: all 0.2s;
        box-shadow: 0 4px 12px rgba(245, 166, 35, 0.2);
      }

      .empty-clear-btn:hover {
        background-color: var(--color-orange);
        color: var(--color-white);
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(230, 126, 34, 0.3);
      }

      .empty-clear-btn:focus-visible {
        outline: 3px solid var(--color-brown);
        outline-offset: 2px;
      }

      /* CARDAPIO-10.02 — Optimized spinner animation */
      .spinner {
        display: inline-block;
        width: 48px;
        height: 48px;
        border: 4px solid rgba(245, 166, 35, 0.16);
        border-radius: 50%;
        border-top-color: var(--color-gold);
        animation: spin 0.8s linear infinite;
        margin-bottom: 20px;
      }

      @keyframes spin {
        to {
          transform: rotate(360deg);
        }
      }

      /* CARDAPIO-07.02 — Safe spacing for floating cart */
      :host {
        --cart-safe-bottom: 120px;
      }

      /* Mobile */
      @media (max-width: 768px) {
        :host {
          --cart-safe-bottom: 140px;
        }

        .public-menu-header {
          min-height: 64px;
          padding: 10px 16px;
        }

        .menu-brand-logo img {
          height: 44px;
          max-width: 140px;
        }

        .menu-nav-link {
          padding: 6px 12px;
          font-size: 0.9rem;
        }

        .menu-hero {
          padding: 40px 16px 24px;
        }

        .menu-hero h1 {
          font-size: clamp(1.6rem, 6vw, 2.2rem);
        }

        .menu-hero p {
          font-size: 1rem;
        }

        .availability-note {
          margin: 12px 16px 24px;
          font-size: 0.85rem;
        }

        .menu-container {
          padding: 0 16px 120px;
        }

        .search-field {
          min-height: 48px;
          padding: 6px 14px;
        }

        .category-chips {
          padding: 0 16px;
          margin: 0 auto 28px;
          gap: 8px;
          justify-content: flex-start;
          overflow-x: auto;
          -webkit-overflow-scrolling: touch;
          scroll-behavior: smooth;
          scrollbar-width: none;
        }

        .category-chips::-webkit-scrollbar {
          display: none;
        }

        .category-chip {
          padding: 8px 14px;
          font-size: 0.85rem;
          min-height: 36px;
          flex-shrink: 0;
        }

        .category-section {
          padding: 0 16px;
          margin-bottom: 40px;
        }

        .category-title {
          font-size: clamp(1.2rem, 4vw, 1.6rem);
        }

        .category-header {
          margin-bottom: 20px;
        }

        .products-grid {
          grid-template-columns: 1fr;
          gap: 16px;
        }

        .loading-state,
        .empty-state {
          margin: 32px 16px;
          padding: 32px 20px;
          margin-bottom: 120px;
        }
      }
    `
  ]
})
export class PublicMenuComponent implements OnInit {
  baseMenu: PublicMenuResponse[] = [];
  menu: PublicMenuResponse[] = [];
  loading = false;
  searchQuery = '';
  selectedCategoryId: string | null = null;
  private searchSubject = new Subject<string>();

  constructor(
    private readonly menuService: PublicMenuService,
    private readonly titleService: Title,
    private readonly metaService: Meta
  ) {
    this.searchSubject.pipe(debounceTime(300), distinctUntilChanged()).subscribe((search) => {
      this.loadMenu(search);
    });
  }

  ngOnInit(): void {
    this.titleService.setTitle('Cardápio Online | Padaria Nova Aliança');
    this.metaService.updateTag({
      name: 'description',
      content: 'Cardápio online da Padaria Nova Aliança. Conheça nossos produtos frescos como pão, café, bolo, salgados e muito mais.'
    });
    this.loadMenu();
  }

  onSearchChange(search: string): void {
    this.searchSubject.next(search);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchSubject.next('');
  }

  onCategoryChange(categoryId: string | null): void {
    this.selectedCategoryId = categoryId;
    this.applyFilters();
  }

  private loadMenu(search?: string): void {
    this.loading = true;
    this.menuService.getMenu(search).subscribe({
      next: (data) => {
        this.baseMenu = data;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.baseMenu = [];
        this.menu = [];
        this.loading = false;
      }
    });
  }

  private applyFilters(): void {
    if (this.selectedCategoryId === null) {
      this.menu = this.baseMenu;
    } else {
      this.menu = this.baseMenu.filter((item) => item.category.id === this.selectedCategoryId);
    }
  }

  getAvailableCategories(): PublicMenuResponse[] {
    return this.baseMenu;
  }
}
