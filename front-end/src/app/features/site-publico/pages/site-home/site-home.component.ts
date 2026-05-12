import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Title, Meta } from '@angular/platform-browser';
import { PublicMenuService } from '../../data-access/public-menu.service';
import { StoreInfoResponse } from '../../domain/public-menu.model';
import {
  FeaturedProduct,
  OrderCategory,
  BenefitItem,
  featuredProducts,
  orderCategories,
  benefits
} from './site-home-content';

@Component({
  selector: 'app-site-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header class="public-header" id="topo">
      <div class="brand">{{ storeInfo?.name || 'Padaria Nova Aliança' }}</div>

      <div class="spacer"></div>

      <nav class="nav-links" aria-label="Navegação principal da home">
        <a href="#cardapio" class="nav-link">Cardápio</a>
        <a href="#encomendas" class="nav-link hidden-on-mobile">Encomendas</a>
        <a href="#contato" class="nav-link hidden-on-mobile">WhatsApp</a>
        <a href="#localizacao" class="nav-link hidden-on-mobile">Como chegar</a>
        <a
          class="nav-link whatsapp-header-btn"
          [href]="whatsappUrl"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Pedir pelo WhatsApp da Padaria Nova Aliança"
        >
          Pedir pelo WhatsApp
        </a>
      </nav>
    </header>

    <div class="hero-section">
      <div class="hero-layout">
        <div class="hero-text">
          <h1>{{ heroTitle }}</h1>

          <p class="subtitle">
            {{ heroSubtitle }}
          </p>

          <div class="cta-group">
            <a class="main-cta" routerLink="/cardapio">Ver Cardápio</a>
            <a
              class="secondary-cta"
              [href]="whatsappUrl"
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Pedir pelo WhatsApp da Padaria Nova Aliança"
            >
              Pedir pelo WhatsApp
            </a>
          </div>
        </div>

        <div class="hero-image">
          <img
            [src]="heroImageUrl"
            alt="Pães, lanches e bolos frescos da Padaria Nova Aliança"
            loading="eager"
            decoding="async"
          />
        </div>
      </div>
    </div>

    <div class="future-anchor-section"></div>

    <div class="info-section">
      <div class="info-grid" *ngIf="storeInfo">
        <a
          class="info-card info-card-link"
          [href]="googleMapsUrl"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Abrir localização da Padaria e Lanchonete Nova Aliança no Google Maps"
        >
          <div class="icon" aria-hidden="true">📍</div>
          <h3>Onde Estamos</h3>
          <p>{{ displayAddress }}</p>
          <span class="maps-link">Abrir no Google Maps</span>
        </a>

        <div class="info-card hours-card">
          <div class="icon" aria-hidden="true">⏰</div>
          <h3>Horário de Funcionamento</h3>

          <div class="hours-content">
            <span class="hours-days">{{ displayOpeningDays }}</span>
            <strong class="hours-time">{{ displayOpeningHours }}</strong>
            <span class="hours-note" *ngIf="openingHoursNote">{{ openingHoursNote }}</span>

            <div class="store-status" [class.store-status-open]="isOpenNow" [class.store-status-closed]="!isOpenNow">
              <span class="store-status-dot"></span>

              <div class="store-status-text">
                <strong>{{ storeStatusLabel }}</strong>
                <span>{{ storeStatusDetail }}</span>
              </div>
            </div>
          </div>
        </div>

        <a
          id="contato"
          class="info-card info-card-link"
          [href]="whatsappUrl"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Abrir conversa com a Padaria Nova Aliança no WhatsApp"
        >
          <div class="icon" aria-hidden="true">📞</div>
          <h3>Contato</h3>
          <p>{{ storeInfo.phone }}</p>
          <span class="whatsapp-link">Chamar no WhatsApp</span>
        </a>
      </div>

      <div class="loading-state" *ngIf="loading">
        <div class="spinner"></div>
        <p>Carregando informações...</p>
      </div>
    </div>

    <div id="cardapio" class="featured-products-section">
      <div class="featured-products-container">
        <h2>Mais Pedidos da Nova Aliança</h2>
        <p class="featured-products-subtitle">
          Conheça os produtos mais populares entre nossos clientes
        </p>

        <div class="featured-products-grid">
          <div class="featured-product-card" *ngFor="let product of featuredProducts">
            <div class="product-icon" aria-hidden="true">{{ product.icon }}</div>
            <h3>{{ product.name }}</h3>
            <p>{{ product.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <section class="delivery-section" id="delivery">
      <div class="delivery-content">
        <div class="delivery-text">
          <span class="section-eyebrow">Delivery</span>
          <h2>Delivery todos os dias</h2>
          <p>
            Receba pães, lanches, bolos, salgados e bebidas no conforto da sua casa.
            Chame no WhatsApp e consulte a disponibilidade para sua região.
          </p>
          <span class="delivery-note">
            Consulte taxa de entrega e disponibilidade para o seu endereço.
          </span>
        </div>

        <div class="delivery-card">
          <div class="delivery-icon" aria-hidden="true">🛵</div>
          <strong>Peça sem sair de casa</strong>
          <span>
            Atendimento rápido pelo WhatsApp para consultar produtos, taxa e região de entrega.
          </span>
          <a
            class="delivery-whatsapp-btn"
            [href]="whatsappUrl"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Pedir delivery pelo WhatsApp da Padaria Nova Aliança"
          >
            Pedir pelo WhatsApp
          </a>
        </div>
      </div>
    </section>

    <section class="orders-section" id="encomendas">
      <div class="orders-content">
        <div class="section-header">
          <span class="section-eyebrow">Encomendas</span>
          <h2>Encomendas para festas, cafés e momentos especiais</h2>
          <p>
            Faça sua encomenda de tortas, bolos, salgados, rocamboles e kits de café com antecedência pelo WhatsApp.
          </p>
        </div>

        <div class="orders-categories-grid">
          <article class="order-category-card" *ngFor="let category of orderCategories">
            <div class="order-category-emoji" aria-hidden="true">{{ category.emoji }}</div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.description }}</p>
          </article>
        </div>

        <div class="orders-actions">
          <a
            class="orders-whatsapp-btn"
            [href]="whatsappOrderUrl"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Fazer encomenda pelo WhatsApp da Padaria Nova Aliança"
          >
            Fazer encomenda pelo WhatsApp
          </a>
        </div>
      </div>
    </section>

    <section class="benefits-section">
      <div class="section-header">
        <span class="section-eyebrow">Diferenciais</span>
        <h2>Por que escolher a Nova Aliança?</h2>
        <p>
          Uma padaria feita para o dia a dia da sua família, com atendimento próximo,
          produtos frescos e opções para café, lanche e encomendas.
        </p>
      </div>

      <div class="benefits-grid">
        <article class="benefit-card" *ngFor="let benefit of benefits">
          <div class="benefit-icon" aria-hidden="true">{{ benefit.icon }}</div>
          <h3>{{ benefit.title }}</h3>
          <p>{{ benefit.description }}</p>
        </article>
      </div>
    </section>

    <section class="location-section" id="localizacao">
      <div class="location-content">
        <div class="location-text">
          <span class="section-eyebrow">Localização</span>
          <h2>Estamos em Parque Veneza, Magé</h2>
          <p>
            Passe na Padaria e Lanchonete Nova Aliança ou trace sua rota pelo Google Maps.
          </p>
        </div>

        <div class="location-card">
          <div class="location-icon" aria-hidden="true">📍</div>
          <div class="location-address">
            <span>Endereço</span>
            <strong>{{ displayAddress }}</strong>
          </div>
          <a
            class="location-maps-btn"
            [href]="googleMapsUrl"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Traçar rota para a Padaria Nova Aliança no Google Maps"
          >
            Traçar rota no Google Maps
          </a>
        </div>
      </div>
    </section>

    <footer class="public-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <h2>Padaria e Lanchonete Nova Aliança</h2>
          <p>Produtos frescos, lanches, encomendas e atendimento pelo WhatsApp.</p>
        </div>

        <div class="footer-info">
          <p><strong>Endereço:</strong> Estr. Mineira, 703 - Parque Veneza, Magé - RJ</p>
          <p><strong>Horário:</strong> Segunda a Domingo: 05:30 às 21:00</p>
          <p><strong>WhatsApp:</strong> {{ storeInfo?.phone || '(21) 99999-9999' }}</p>
        </div>

        <nav class="footer-links" aria-label="Links úteis do rodapé">
          <a routerLink="/cardapio">Cardápio</a>
          <a
            [href]="whatsappUrl"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Abrir conversa com a Padaria Nova Aliança no WhatsApp"
          >
            WhatsApp
          </a>
          <a
            [href]="googleMapsUrl"
            target="_blank"
            rel="noopener noreferrer"
            aria-label="Traçar rota para a Padaria Nova Aliança no Google Maps"
          >
            Como chegar
          </a>
          <a routerLink="/login" class="internal-area-link">Área interna</a>
        </nav>
      </div>

      <div class="footer-bottom">
        © {{ currentYear }} Padaria e Lanchonete Nova Aliança. Todos os direitos reservados.
      </div>
    </footer>

    <a
      class="back-to-top"
      href="#topo"
      aria-label="Voltar para o topo da página"
    >
      ↑ Topo
    </a>

    <a
      class="floating-whatsapp"
      [href]="whatsappUrl"
      target="_blank"
      rel="noopener noreferrer"
      aria-label="Pedir pelo WhatsApp da Padaria Nova Aliança"
    >
      💬 Pedir pelo WhatsApp
    </a>
  `,
  styles: [
    `
      html {
        scroll-behavior: smooth;
      }

      #topo,
      #cardapio,
      #encomendas,
      #contato,
      #localizacao {
        scroll-margin-top: 96px;
      }

      .public-header {
        display: flex;
        align-items: center;
        padding: 16px 24px;
        background-color: #6b3f1d;
        color: white;
        flex-wrap: wrap;
        gap: 12px;
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
        flex-wrap: wrap;
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

      .nav-link:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .whatsapp-header-btn {
        background-color: #25d366;
        color: white;
        padding: 10px 20px;
        border-radius: 8px;
        font-weight: 600;
      }

      .whatsapp-header-btn:hover {
        background-color: #1ead4f;
        transform: translateY(-2px);
      }

      .whatsapp-header-btn:focus-visible {
        outline: 2px solid white;
        outline-offset: 2px;
      }

      .hidden-on-mobile {
        display: none;
      }

      @media (min-width: 769px) {
        .hidden-on-mobile {
          display: inline-block !important;
        }
      }

      .hero-section {
        background: linear-gradient(135deg, #6b3f1d 0%, #9c6b3f 55%, #e67e22 100%);
        color: white;
        padding: 72px 24px;
        min-height: 480px;
        display: flex;
        align-items: center;
      }

      .hero-layout {
        width: 100%;
        max-width: 1200px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: minmax(0, 1fr) minmax(320px, 480px);
        gap: 48px;
        align-items: center;
      }

      .hero-text {
        text-align: left;

        h1 {
          font-size: 3.5rem;
          font-weight: 800;
          margin-bottom: 24px;
          line-height: 1.2;

          .highlight {
            color: #f5a623;
          }
        }

        .subtitle {
          font-size: 1.25rem;
          opacity: 1;
          margin-bottom: 40px;
          line-height: 1.6;
          color: #fff7e8;
        }
      }

      .hero-image {
        display: flex;
        justify-content: center;
      }

      .hero-image img {
        width: 100%;
        max-width: 480px;
        max-height: 360px;
        object-fit: contain;
        border-radius: 24px;
        padding: 24px;
        background-color: rgba(255, 247, 232, 0.12);
        box-shadow: 0 24px 60px rgba(61, 43, 31, 0.22);
      }

      .cta-group {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 16px;
        flex-wrap: wrap;
      }

      .main-cta {
        display: inline-block;
        padding: 12px 32px;
        font-size: 1.1rem;
        border-radius: 30px;
        background-color: #f5a623;
        color: #3d2b1f;
        text-decoration: none;
        font-weight: 600;
        transition:
          background-color 0.2s,
          transform 0.2s;
      }

      .main-cta:hover {
        background-color: #e67e22;
        color: white;
        transform: translateY(-2px);
      }

      .main-cta:focus-visible {
        outline: 3px solid #3d2b1f;
        outline-offset: 4px;
      }

      .secondary-cta {
        display: inline-block;
        padding: 12px 32px;
        font-size: 1.1rem;
        border-radius: 30px;
        background-color: #1e7e34;
        color: white;
        text-decoration: none;
        font-weight: 600;
        transition:
          background-color 0.2s,
          transform 0.2s;
      }

      .secondary-cta:hover {
        background-color: #16662a;
        transform: translateY(-2px);
      }

      .secondary-cta:focus-visible {
        outline: 3px solid white;
        outline-offset: 4px;
      }

      .future-anchor-section {
        padding: 1px;
        visibility: hidden;
      }

      .info-section {
        padding: 64px 24px;
        background-color: #fff7e8;
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
        color: inherit;
        text-decoration: none;

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
          color: #3d2b1f;
          margin-bottom: 12px;
          font-weight: 600;
        }

        p {
          color: #6b3f1d;
          line-height: 1.6;
          margin: 0;
        }
      }

      .info-card-link {
        display: block;
        cursor: pointer;
      }

      .info-card-link:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .maps-link {
        display: inline-block;
        margin-top: 12px;
        color: #e67e22;
        font-weight: 700;
        font-size: 0.95rem;
      }

      .whatsapp-link {
        display: inline-block;
        margin-top: 12px;
        color: #1e7e34;
        font-weight: 700;
        font-size: 0.95rem;
      }

      .info-card-link:hover .maps-link,
      .info-card-link:hover .whatsapp-link {
        text-decoration: underline;
      }

      .hours-card {
        display: flex;
        flex-direction: column;
        align-items: center;
      }

      .hours-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        margin-top: 4px;
      }

      .hours-days {
        color: #6b3f1d;
        font-weight: 600;
        font-size: 1rem;
      }

      .hours-time {
        color: #3d2b1f;
        font-size: 1.45rem;
        font-weight: 800;
        line-height: 1.2;
      }

      .hours-note {
        margin-top: 4px;
        color: #9c6b3f;
        font-size: 0.9rem;
        line-height: 1.4;
      }

      .store-status {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        margin-top: 16px;
        padding: 10px 14px;
        border-radius: 999px;
        font-weight: 600;
      }

      .store-status-open {
        background-color: rgba(30, 126, 52, 0.12);
        color: #1e7e34;
      }

      .store-status-closed {
        background-color: rgba(107, 63, 29, 0.1);
        color: #6b3f1d;
      }

      .store-status-dot {
        width: 10px;
        height: 10px;
        border-radius: 999px;
        background-color: currentColor;
        flex: 0 0 auto;
      }

      .store-status-text {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        line-height: 1.2;
      }

      .store-status-text span {
        font-size: 0.85rem;
        font-weight: 500;
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
        border-top-color: #6b3f1d;
        animation: spin 1s ease-in-out infinite;
        margin-bottom: 16px;
      }

      @keyframes spin {
        to {
          transform: rotate(360deg);
        }
      }

      .public-footer {
        background-color: #3d2b1f;
        color: #fff7e8;
        padding: 48px 24px 24px;
      }

      .footer-content {
        max-width: 1200px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: 1.2fr 1fr auto;
        gap: 32px;
        align-items: start;
      }

      .footer-brand h2 {
        margin: 0 0 12px 0;
        font-size: 1.4rem;
        color: #ffffff;
        font-weight: 700;
      }

      .footer-brand p,
      .footer-info p {
        margin: 0 0 10px 0;
        color: #fff7e8;
        line-height: 1.6;
        font-size: 0.95rem;
      }

      .footer-links {
        display: flex;
        flex-direction: column;
        gap: 10px;
      }

      .footer-links a {
        color: #fff7e8;
        text-decoration: none;
        font-weight: 700;
        font-size: 0.95rem;
        transition: color 0.2s;
      }

      .footer-links a:hover {
        color: #f5a623;
        text-decoration: underline;
      }

      .footer-links a:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .internal-area-link {
        opacity: 0.75;
        font-size: 0.95rem;
      }

      .footer-bottom {
        max-width: 1200px;
        margin: 32px auto 0;
        padding-top: 20px;
        border-top: 1px solid rgba(255, 247, 232, 0.18);
        color: rgba(255, 247, 232, 0.8);
        font-size: 0.9rem;
        text-align: center;
      }

      .floating-whatsapp {
        position: fixed;
        right: 24px;
        bottom: 24px;
        z-index: 1000;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-height: 52px;
        padding: 14px 22px;
        border-radius: 999px;
        background-color: #1e7e34;
        color: #ffffff;
        text-decoration: none;
        font-weight: 800;
        box-shadow: 0 14px 36px rgba(30, 126, 52, 0.3);
        transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
      }

      .floating-whatsapp:hover {
        background-color: #16662a;
        transform: translateY(-2px);
        box-shadow: 0 18px 42px rgba(30, 126, 52, 0.36);
      }

      .floating-whatsapp:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .back-to-top {
        position: fixed;
        left: 24px;
        bottom: 24px;
        z-index: 999;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        min-height: 44px;
        padding: 10px 16px;
        border-radius: 999px;
        background-color: #6b3f1d;
        color: #ffffff;
        text-decoration: none;
        font-weight: 800;
        box-shadow: 0 10px 28px rgba(61, 43, 31, 0.22);
        transition:
          background-color 0.2s,
          transform 0.2s,
          box-shadow 0.2s;
      }

      .back-to-top:hover {
        background-color: #3d2b1f;
        transform: translateY(-2px);
        box-shadow: 0 14px 34px rgba(61, 43, 31, 0.28);
      }

      .back-to-top:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .delivery-section {
        padding: 72px 24px;
        background: linear-gradient(135deg, #fff7e8 0%, #ffffff 100%);
      }

      .delivery-content {
        max-width: 1200px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: minmax(0, 1.2fr) minmax(300px, 0.8fr);
        gap: 32px;
        align-items: center;
      }

      .delivery-text {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .section-eyebrow {
        color: #e67e22;
        font-weight: 700;
        font-size: 0.9rem;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .delivery-text h2 {
        margin: 0;
        color: #3d2b1f;
        font-size: 2.3rem;
        line-height: 1.15;
      }

      .delivery-text p {
        margin: 0;
        color: #6b3f1d;
        font-size: 1.1rem;
        line-height: 1.7;
      }

      .delivery-note {
        display: inline-flex;
        padding: 10px 14px;
        border-radius: 999px;
        background-color: rgba(245, 166, 35, 0.16);
        color: #6b3f1d;
        font-weight: 700;
        font-size: 0.95rem;
        align-self: flex-start;
      }

      .delivery-card {
        background-color: #6b3f1d;
        color: #ffffff;
        border-radius: 24px;
        padding: 32px;
        box-shadow: 0 18px 48px rgba(61, 43, 31, 0.18);
        display: flex;
        flex-direction: column;
        gap: 14px;
      }

      .delivery-icon {
        font-size: 3rem;
      }

      .delivery-card strong {
        font-size: 1.4rem;
        line-height: 1.2;
        margin: 0;
      }

      .delivery-card span {
        color: #fff7e8;
        line-height: 1.6;
        margin: 0;
      }

      .delivery-whatsapp-btn {
        display: inline-flex;
        justify-content: center;
        align-items: center;
        min-height: 46px;
        margin-top: 8px;
        padding: 12px 22px;
        border-radius: 999px;
        background-color: #1e7e34;
        color: #ffffff;
        text-decoration: none;
        font-weight: 800;
        transition:
          background-color 0.2s,
          transform 0.2s;
      }

      .delivery-whatsapp-btn:hover {
        background-color: #16662a;
        transform: translateY(-2px);
      }

      .delivery-whatsapp-btn:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .orders-section {
        padding: 72px 24px;
        background-color: #ffffff;
      }

      .orders-content {
        max-width: 1200px;
        margin: 0 auto;
      }

      .orders-content > .section-header h2 {
        font-size: 2.3rem;
        color: #3d2b1f;
        margin: 0 0 16px 0;
        font-weight: 700;
      }

      .orders-content > .section-header p {
        font-size: 1.1rem;
        color: #6b3f1d;
        line-height: 1.7;
        margin: 0 0 40px 0;
      }

      .orders-categories-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 24px;
        margin-top: 0;
      }

      .order-category-card {
        background-color: #fff7e8;
        border: 1px solid rgba(156, 107, 63, 0.18);
        border-radius: 20px;
        padding: 26px;
        text-align: center;
        box-shadow: 0 10px 28px rgba(61, 43, 31, 0.08);
        transition: transform 0.2s ease, box-shadow 0.2s ease;
      }

      .order-category-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 16px 38px rgba(61, 43, 31, 0.14);
      }

      .order-category-emoji {
        font-size: 2.4rem;
        margin-bottom: 14px;
      }

      .order-category-card h3 {
        margin: 0 0 10px 0;
        color: #3d2b1f;
        font-size: 1.25rem;
        font-weight: 600;
      }

      .order-category-card p {
        margin: 0;
        color: #6b3f1d;
        line-height: 1.6;
        font-size: 0.95rem;
      }

      .orders-actions {
        margin-top: 36px;
        display: flex;
        justify-content: center;
      }

      .orders-whatsapp-btn {
        display: inline-flex;
        justify-content: center;
        align-items: center;
        min-height: 48px;
        padding: 14px 28px;
        border-radius: 999px;
        background-color: #1e7e34;
        color: #ffffff;
        text-decoration: none;
        font-weight: 800;
        box-shadow: 0 10px 24px rgba(30, 126, 52, 0.2);
        transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
      }

      .orders-whatsapp-btn:hover {
        background-color: #16662a;
        transform: translateY(-2px);
        box-shadow: 0 14px 30px rgba(30, 126, 52, 0.26);
      }

      .orders-whatsapp-btn:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .benefits-section {
        padding: 72px 24px;
        background-color: #fff7e8;
      }

      .benefits-section > .section-header {
        max-width: 1200px;
        margin: 0 auto 40px;
      }

      .benefits-section > .section-header h2 {
        font-size: 2.5rem;
        color: #3d2b1f;
        margin: 0 0 16px 0;
        font-weight: 700;
      }

      .benefits-section > .section-header p {
        font-size: 1.1rem;
        color: #6b3f1d;
        line-height: 1.7;
        margin: 0;
      }

      .benefits-grid {
        max-width: 1200px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: repeat(5, minmax(0, 1fr));
        gap: 18px;
      }

      .benefit-card {
        background-color: #ffffff;
        border-radius: 20px;
        padding: 24px;
        text-align: center;
        border: 1px solid rgba(156, 107, 63, 0.16);
        box-shadow: 0 10px 28px rgba(61, 43, 31, 0.08);
        transition: transform 0.2s ease, box-shadow 0.2s ease;
      }

      .benefit-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 16px 36px rgba(61, 43, 31, 0.14);
      }

      .benefit-icon {
        font-size: 2.2rem;
        margin-bottom: 14px;
      }

      .benefit-card h3 {
        margin: 0 0 10px 0;
        color: #3d2b1f;
        font-size: 1.05rem;
        line-height: 1.25;
        font-weight: 600;
      }

      .benefit-card p {
        margin: 0;
        color: #6b3f1d;
        font-size: 0.94rem;
        line-height: 1.55;
      }

      .location-section {
        padding: 72px 24px;
        background-color: #fff7e8;
      }

      .location-content {
        max-width: 1200px;
        margin: 0 auto;
        display: grid;
        grid-template-columns: minmax(0, 1fr) minmax(320px, 460px);
        gap: 32px;
        align-items: center;
      }

      .location-text {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }

      .location-text h2 {
        margin: 0;
        color: #3d2b1f;
        font-size: 2.3rem;
        line-height: 1.15;
        font-weight: 700;
      }

      .location-text p {
        margin: 0;
        color: #6b3f1d;
        font-size: 1.1rem;
        line-height: 1.7;
      }

      .location-card {
        background-color: #ffffff;
        border: 1px solid rgba(156, 107, 63, 0.18);
        border-radius: 24px;
        padding: 32px;
        box-shadow: 0 14px 36px rgba(61, 43, 31, 0.12);
        display: flex;
        flex-direction: column;
        gap: 18px;
      }

      .location-icon {
        font-size: 3rem;
      }

      .location-address {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .location-address span {
        color: #e67e22;
        font-weight: 800;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        font-size: 0.8rem;
      }

      .location-address strong {
        color: #3d2b1f;
        font-size: 1.15rem;
        line-height: 1.5;
        font-weight: 700;
      }

      .location-maps-btn {
        display: inline-flex;
        justify-content: center;
        align-items: center;
        min-height: 48px;
        padding: 14px 24px;
        border-radius: 999px;
        background-color: #f5a623;
        color: #3d2b1f;
        text-decoration: none;
        font-weight: 800;
        transition: background-color 0.2s, color 0.2s, transform 0.2s;
      }

      .location-maps-btn:hover {
        background-color: #e67e22;
        color: #ffffff;
        transform: translateY(-2px);
      }

      .location-maps-btn:focus-visible {
        outline: 3px solid #6b3f1d;
        outline-offset: 4px;
      }

      @media (max-width: 1100px) {
        .benefits-grid {
          grid-template-columns: repeat(3, minmax(0, 1fr));
        }
      }

      .featured-products-section {
        background-color: white;
        padding: 64px 24px;
      }

      .featured-products-container {
        max-width: 1200px;
        margin: 0 auto;
      }

      .featured-products-section h2 {
        font-size: 2.5rem;
        color: #3d2b1f;
        text-align: center;
        margin: 0 0 12px 0;
        font-weight: 700;
      }

      .featured-products-subtitle {
        font-size: 1.1rem;
        color: #6b3f1d;
        text-align: center;
        margin: 0 0 48px 0;
        opacity: 0.9;
      }

      .featured-products-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 32px;
      }

      .featured-product-card {
        background: #fff7e8;
        padding: 32px 24px;
        border-radius: 16px;
        text-align: center;
        transition: transform 0.3s ease, box-shadow 0.3s ease;
        cursor: pointer;
      }

      .featured-product-card:hover {
        transform: translateY(-8px);
        box-shadow: 0 12px 32px rgba(107, 63, 29, 0.15);
      }

      .product-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }

      .featured-product-card h3 {
        font-size: 1.25rem;
        color: #3d2b1f;
        margin: 0 0 12px 0;
        font-weight: 600;
      }

      .featured-product-card p {
        color: #6b3f1d;
        font-size: 0.95rem;
        line-height: 1.6;
        margin: 0;
      }

      @media (max-width: 768px) {
        .public-header {
          padding: 12px 16px;
          gap: 8px;
          align-items: center;
        }

        .brand {
          font-size: 1rem;
          flex-shrink: 0;
        }

        .spacer {
          display: none;
        }

        .nav-links {
          display: flex;
          gap: 8px;
          align-items: center;
          flex-wrap: nowrap;
        }

        .nav-link {
          padding: 8px 12px;
          font-size: 0.85rem;
          white-space: nowrap;
        }

        .hidden-on-mobile {
          display: none !important;
        }

        .whatsapp-header-btn {
          padding: 10px 14px;
          font-size: 0.85rem;
          flex-shrink: 0;
          margin-left: auto;
        }

        .hero-section {
          padding: 48px 16px;
        }

        .hero-section {
          padding: 40px 16px 48px;
        }

        .hero-layout {
          display: flex;
          flex-direction: column;
          gap: 28px;
        }

        .hero-image {
          order: 1;
          width: 100%;
        }

        .hero-text {
          order: 2;
          text-align: center;
        }

        .hero-text h1 {
          font-size: 2.5rem;
        }

        .hero-image img {
          max-width: 320px;
          max-height: 260px;
          border-radius: 20px;
          padding: 18px;
        }

        .cta-group {
          flex-direction: column;
          width: 100%;
          max-width: 320px;
          margin: 0 auto;
        }

        .main-cta,
        .secondary-cta {
          width: 100%;
          padding: 12px 24px;
          text-align: center;
        }

        .hours-time {
          font-size: 1.3rem;
        }

        .hours-note {
          font-size: 0.85rem;
        }

        .store-status {
          max-width: 100%;
        }

        .store-status-text {
          align-items: center;
        }

        .info-grid {
          grid-template-columns: 1fr;
        }

        .delivery-section {
          padding: 56px 16px;
        }

        .delivery-content {
          grid-template-columns: 1fr;
        }

        .delivery-text {
          text-align: center;
        }

        .delivery-text h2 {
          font-size: 1.9rem;
        }

        .delivery-note {
          border-radius: 16px;
          align-self: center;
        }

        .delivery-card {
          padding: 28px 24px;
          text-align: center;
          align-items: center;
        }

        .delivery-whatsapp-btn {
          width: 100%;
        }

        .featured-products-section {
          padding: 48px 16px;
        }

        .featured-products-section h2 {
          font-size: 1.75rem;
        }

        .featured-products-subtitle {
          font-size: 1rem;
          margin-bottom: 32px;
        }

        .featured-products-grid {
          grid-template-columns: repeat(2, 1fr);
          gap: 16px;
        }

        .featured-product-card {
          padding: 20px 16px;
        }

        .product-icon {
          font-size: 36px;
        }

        .featured-product-card h3 {
          font-size: 1.1rem;
        }

        .featured-product-card p {
          font-size: 0.9rem;
        }

        .orders-section {
          padding: 56px 16px;
        }

        .orders-categories-grid {
          grid-template-columns: repeat(2, minmax(0, 1fr));
          gap: 18px;
        }

        .order-category-card {
          padding: 24px;
        }

        .orders-whatsapp-btn {
          width: 100%;
        }

        .benefits-section {
          padding: 56px 16px;
        }

        .benefits-section > .section-header h2 {
          font-size: 1.9rem;
        }

        .benefits-grid {
          grid-template-columns: 1fr;
          gap: 16px;
        }

        .benefit-card {
          padding: 22px;
        }

        .location-section {
          padding: 56px 16px;
        }

        .location-content {
          grid-template-columns: 1fr;
        }

        .location-text {
          text-align: center;
        }

        .location-text h2 {
          font-size: 1.9rem;
        }

        .location-card {
          padding: 28px 24px;
          text-align: center;
          align-items: center;
        }

        .location-maps-btn {
          width: 100%;
        }

        .footer-content {
          grid-template-columns: 1fr;
          text-align: center;
        }

        .footer-links {
          align-items: center;
        }

        .floating-whatsapp {
          left: 16px;
          right: 16px;
          bottom: 16px;
          width: auto;
        }

        .back-to-top {
          left: 16px;
          bottom: 84px;
          min-height: 42px;
          padding: 9px 14px;
          font-size: 0.9rem;
        }

        .public-footer {
          padding-bottom: 96px;
        }
      }

      @media (max-width: 640px) {
        .orders-categories-grid {
          grid-template-columns: 1fr;
        }
      }

      @media (max-width: 480px) {
        .featured-products-grid {
          grid-template-columns: 1fr;
        }
      }

      @media (max-width: 390px) {
        .public-header {
          padding: 12px 16px;
        }

        .brand {
          font-size: 1rem;
          width: 100%;
        }

        .spacer {
          display: none;
        }

        .nav-links {
          width: 100%;
          justify-content: center;
          gap: 8px;
        }

        .nav-link {
          padding: 6px 10px;
          font-size: 0.8rem;
        }

        .whatsapp-header-btn {
          padding: 8px 12px;
          font-size: 0.8rem;
        }

        .hero-text h1 {
          font-size: 2rem;
        }

        .featured-products-grid {
          gap: 16px;
        }

        .featured-product-card {
          padding: 16px 12px;
        }

        .product-icon {
          font-size: 32px;
          margin-bottom: 12px;
        }

        .featured-product-card h3 {
          font-size: 1rem;
        }

        .featured-product-card p {
          font-size: 0.85rem;
        }

        .benefits-grid {
          grid-template-columns: 1fr;
          gap: 12px;
        }

        .benefit-card {
          padding: 16px;
        }

        .benefit-icon {
          font-size: 1.8rem;
        }

        .benefit-card h3 {
          font-size: 0.95rem;
        }

        .benefit-card p {
          font-size: 0.85rem;
        }
      }
    `
  ]
})
export class SiteHomeComponent implements OnInit {
  storeInfo: StoreInfoResponse | null = null;
  loading = true;

  readonly pageTitle = 'Padaria Nova Aliança | Pães, Lanches e Encomendas em Magé';

  readonly pageDescription = 'Padaria e Lanchonete Nova Aliança em Parque Veneza, Magé. Pães frescos, lanches, bolos, salgados, delivery e encomendas pelo WhatsApp.';

  readonly storeAddress = 'Estr. Mineira, 703 - Parque Veneza, Magé - RJ, 25930-790';

  readonly fallbackStoreAddress = 'Estr. Mineira, 703 - Parque Veneza, Magé - RJ, 25930-790';

  readonly fallbackStorePhone = '(21) 96582-4610';

  readonly fallbackOpeningDays = 'Segunda a Domingo';

  readonly fallbackOpeningHours = '05:30 às 21:00';

  readonly openingTime = '05:30';

  readonly closingTime = '21:00';

  readonly heroTitle = 'Pães quentinhos, lanches e bolos frescos todos os dias';

  readonly heroSubtitle =
    'A Padaria e Lanchonete Nova Aliança prepara seu café, lanche e encomendas com qualidade e carinho em Parque Veneza, Magé.';

  readonly heroImageUrl = 'assets/images/site/logo.png';

  readonly featuredProducts = featuredProducts;

  readonly orderCategories = orderCategories;

  readonly benefits = benefits;

  readonly currentYear = new Date().getFullYear();

  get displayAddress(): string {
    const apiAddress = this.storeInfo?.address?.trim();

    return apiAddress && apiAddress.length > 0
      ? apiAddress
      : this.fallbackStoreAddress;
  }

  get displayPhone(): string {
    const apiPhone = this.storeInfo?.phone?.trim();

    return apiPhone && apiPhone.length > 0
      ? apiPhone
      : this.fallbackStorePhone;
  }

  get googleMapsUrl(): string {
    return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(this.displayAddress)}`;
  }

  get displayOpeningDays(): string {
    return this.fallbackOpeningDays;
  }

  get displayOpeningHours(): string {
    const apiHours = this.storeInfo?.hours?.trim();

    if (!apiHours) {
      return this.fallbackOpeningHours;
    }

    if (apiHours.includes('05:30') && apiHours.includes('21:00')) {
      return this.fallbackOpeningHours;
    }

    return this.fallbackOpeningHours;
  }

  get openingHoursNote(): string | null {
    const apiHours = this.storeInfo?.hours?.trim();

    if (!apiHours) {
      return null;
    }

    const normalized = apiHours.toLowerCase();

    if (
      normalized.includes('segunda') &&
      normalized.includes('domingo') &&
      normalized.includes('05:30') &&
      normalized.includes('21:00')
    ) {
      return null;
    }

    return apiHours;
  }

  // MVP: Cálculo simples baseado no horário padrão (Segunda a Domingo, 05:30-21:00)
  // TODO: Feriados, horários especiais e exceções devem ser tratados futuramente
  // por configuração estruturada vinda do back-end
  get isOpenNow(): boolean {
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const openingMinutes = this.timeToMinutes(this.openingTime);
    const closingMinutes = this.timeToMinutes(this.closingTime);

    return currentMinutes >= openingMinutes && currentMinutes < closingMinutes;
  }

  get storeStatusLabel(): string {
    return this.isOpenNow ? 'Aberto agora' : 'Fechado agora';
  }

  get storeStatusDetail(): string {
    if (this.isOpenNow) {
      return `Fecha às ${this.closingTime}`;
    }

    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const openingMinutes = this.timeToMinutes(this.openingTime);

    return currentMinutes < openingMinutes
      ? `Abre às ${this.openingTime}`
      : `Abre amanhã às ${this.openingTime}`;
  }

  private timeToMinutes(time: string): number {
    const [hours, minutes] = time.split(':').map(Number);
    return hours * 60 + minutes;
  }

  get whatsappUrl(): string {
    return this.buildWhatsappUrl(
      'Olá! Vim pelo site da Padaria Nova Aliança e gostaria de fazer um pedido.'
    );
  }

  get whatsappOrderUrl(): string {
    return this.buildWhatsappUrl(
      'Olá! Vim pelo site da Padaria Nova Aliança e gostaria de fazer uma encomenda.'
    );
  }

  private buildWhatsappUrl(message: string): string {
    const digitsOnly = this.displayPhone.replace(/\D/g, '');

    const phoneWithCountryCode = digitsOnly.startsWith('55')
      ? digitsOnly
      : `55${digitsOnly}`;

    return `https://wa.me/${phoneWithCountryCode}?text=${encodeURIComponent(message)}`;
  }

  private setSeoMetadata(): void {
    this.title.setTitle(this.pageTitle);
    this.meta.updateTag({ name: 'description', content: this.pageDescription });
    this.meta.updateTag({ property: 'og:title', content: this.pageTitle });
    this.meta.updateTag({ property: 'og:description', content: this.pageDescription });
    this.meta.updateTag({ property: 'og:type', content: 'business.business' });
  }

  constructor(
    private readonly menuService: PublicMenuService,
    private readonly title: Title,
    private readonly meta: Meta
  ) {}

  ngOnInit(): void {
    this.setSeoMetadata();
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