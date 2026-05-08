import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PublicMenuService } from '../../data-access/public-menu.service';
import { StoreInfoResponse } from '../../domain/public-menu.model';

@Component({
  selector: 'app-site-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header class="public-header">
      <div class="brand">{{ storeInfo?.name || 'Padaria Nova Aliança' }}</div>
      <div class="spacer"></div>
      <nav class="nav-links">
        <a routerLink="/cardapio" class="nav-link">Ver Cardápio</a>
        <a routerLink="/login" class="nav-link nav-btn">Login</a>
      </nav>
    </header>

    <div class="hero-section">
      <div class="hero-content">
        <h1>
          Bem-vindo à <br />
          <span class="highlight">{{ storeInfo?.name || 'Nova Aliança' }}</span>
        </h1>
        <p class="subtitle">
          {{ storeInfo?.description || 'A melhor padaria da região, com pães frescos a toda hora.' }}
        </p>

        <div class="cta-group">
          <a class="main-cta" routerLink="/cardapio"> Ver Nosso Cardápio </a>
        </div>
      </div>
    </div>

    <div class="info-section">
      <div class="info-grid" *ngIf="storeInfo">
        <div class="info-card">
          <div class="icon">📍</div>
          <h3>Onde Estamos</h3>
          <p>{{ storeInfo.address }}</p>
        </div>

        <div class="info-card">
          <div class="icon">⏰</div>
          <h3>Horário de Funcionamento</h3>
          <p>{{ storeInfo.hours }}</p>
        </div>

        <div class="info-card">
          <div class="icon">📞</div>
          <h3>Contato</h3>
          <p>{{ storeInfo.phone }}</p>
        </div>
      </div>

      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <p>Carregando informações...</p>
      </div>
    </div>
  `,
  styles: [
    `
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

      .nav-btn {
        background-color: white;
        color: #1e3c72;
      }

      .nav-btn:hover {
        background-color: #f5f5f5;
      }

      .hero-section {
        background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
        color: white;
        padding: 80px 24px;
        text-align: center;
        min-height: 400px;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .hero-content {
        max-width: 800px;
        margin: 0 auto;

        h1 {
          font-size: 3.5rem;
          font-weight: 800;
          margin-bottom: 24px;
          line-height: 1.2;

          .highlight {
            color: #f39c12;
          }
        }

        .subtitle {
          font-size: 1.25rem;
          opacity: 0.9;
          margin-bottom: 40px;
          line-height: 1.6;
        }
      }

      .main-cta {
        display: inline-block;
        padding: 12px 32px;
        font-size: 1.1rem;
        border-radius: 30px;
        background-color: #f39c12;
        color: white;
        text-decoration: none;
        font-weight: 600;
        transition:
          background-color 0.2s,
          transform 0.2s;
      }

      .main-cta:hover {
        background-color: #e67e22;
        transform: translateY(-2px);
      }

      .info-section {
        padding: 64px 24px;
        background-color: #f8f9fa;
      }

      .info-grid {
        max-width: 1200px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
        gap: 32px;
      }

      .info-card {
        background: white;
        padding: 32px;
        border-radius: 16px;
        text-align: center;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
        transition: transform 0.3s ease;

        &:hover {
          transform: translateY(-8px);
          box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
        }

        .icon {
          font-size: 48px;
          margin-bottom: 16px;
        }

        h3 {
          font-size: 1.25rem;
          color: #2c3e50;
          margin-bottom: 12px;
          font-weight: 600;
        }

        p {
          color: #7f8c8d;
          line-height: 1.6;
          margin: 0;
        }
      }

      .loading-state {
        text-align: center;
        color: #95a5a6;
        padding: 48px;
      }

      .spinner {
        display: inline-block;
        width: 40px;
        height: 40px;
        border: 4px solid rgba(0, 0, 0, 0.1);
        border-radius: 50%;
        border-top-color: #1e3c72;
        animation: spin 1s ease-in-out infinite;
        margin-bottom: 16px;
      }

      @keyframes spin {
        to {
          transform: rotate(360deg);
        }
      }

      @media (max-width: 768px) {
        .hero-section {
          padding: 48px 16px;
        }

        .hero-content h1 {
          font-size: 2.5rem;
        }
      }
    `
  ]
})
export class SiteHomeComponent implements OnInit {
  storeInfo: StoreInfoResponse | null = null;
  loading = true;

  constructor(private readonly menuService: PublicMenuService) {}

  ngOnInit(): void {
    this.menuService.getStoreInfo().subscribe({
      next: (info) => {
        this.storeInfo = info;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
