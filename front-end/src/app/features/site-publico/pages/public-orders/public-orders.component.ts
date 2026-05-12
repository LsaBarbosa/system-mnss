import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

interface OrderCategory {
  name: string;
  description: string;
  emoji: string;
}

@Component({
  selector: 'app-public-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <header class="public-header">
      <a routerLink="/" class="brand-back">← Voltar</a>
      <div class="spacer"></div>
      <nav class="nav-links">
        <a routerLink="/cardapio" class="nav-link">Cardápio</a>
      </nav>
    </header>

    <div class="orders-container">
      <div class="orders-header">
        <h1>Encomendas</h1>
        <p class="subtitle">
          Escolha uma categoria de encomenda e veja as opções disponíveis. Tire suas dúvidas pelo WhatsApp.
        </p>
      </div>

      <div class="orders-grid">
        <article class="order-card" *ngFor="let category of categories">
          <div class="order-emoji" aria-hidden="true">{{ category.emoji }}</div>
          <h3>{{ category.name }}</h3>
          <p>{{ category.description }}</p>
          <a class="order-btn" [href]="getDoubtUrl(category.name)" target="_blank" rel="noopener noreferrer">
            Tirar dúvidas sobre encomenda
          </a>
        </article>
      </div>

      <div class="orders-info">
        <p>
          As encomendas são personalizadas e preparadas com carinho.
          Entre em contato pelo WhatsApp para consultar disponibilidade, sabores e valores.
        </p>
      </div>
    </div>
  `,
  styles: [
    `
      .public-header {
        display: flex;
        align-items: center;
        padding: 12px 24px;
        background-color: #6b3f1d;
        color: white;
        min-height: 80px;
      }

      .brand-back {
        font-size: 1rem;
        font-weight: 700;
        color: white;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 8px 16px;
        border-radius: 8px;
        transition: background-color 0.2s;
      }

      .brand-back:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }

      .brand-back:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
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
        font-weight: 600;
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

      .orders-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 64px 24px;
      }

      .orders-header {
        text-align: center;
        margin-bottom: 64px;
      }

      .orders-header h1 {
        font-size: 3rem;
        color: #3d2b1f;
        margin-bottom: 16px;
        font-weight: 800;
        line-height: 1.1;
      }

      .subtitle {
        font-size: 1.15rem;
        color: #6b3f1d;
        line-height: 1.7;
        max-width: 600px;
        margin: 0 auto;
      }

      .orders-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 24px;
        margin-bottom: 64px;
      }

      .order-card {
        background: #fff7e8;
        border: 1px solid rgba(156, 107, 63, 0.18);
        border-radius: 20px;
        padding: 28px 24px;
        text-align: center;
        box-shadow: 0 10px 28px rgba(61, 43, 31, 0.08);
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        display: flex;
        flex-direction: column;
        gap: 12px;
      }

      .order-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 16px 38px rgba(61, 43, 31, 0.14);
      }

      .order-emoji {
        font-size: 2.8rem;
        line-height: 1;
      }

      .order-card h3 {
        margin: 0;
        color: #3d2b1f;
        font-size: 1.3rem;
        font-weight: 600;
      }

      .order-card p {
        margin: 0;
        color: #6b3f1d;
        font-size: 0.95rem;
        line-height: 1.6;
      }

      .order-btn {
        display: inline-flex;
        justify-content: center;
        align-items: center;
        min-height: 44px;
        padding: 10px 16px;
        margin-top: 12px;
        border-radius: 999px;
        background-color: #1e7e34;
        color: #ffffff;
        text-decoration: none;
        font-weight: 700;
        font-size: 0.9rem;
        box-shadow: 0 6px 16px rgba(30, 126, 52, 0.2);
        transition: background-color 0.2s, transform 0.2s, box-shadow 0.2s;
      }

      .order-btn:hover {
        background-color: #16662a;
        transform: translateY(-2px);
        box-shadow: 0 10px 24px rgba(30, 126, 52, 0.26);
      }

      .order-btn:focus-visible {
        outline: 3px solid #f5a623;
        outline-offset: 4px;
      }

      .orders-info {
        background: #ffffff;
        border: 1px solid rgba(156, 107, 63, 0.18);
        border-radius: 20px;
        padding: 32px;
        text-align: center;
        box-shadow: 0 10px 28px rgba(61, 43, 31, 0.08);
      }

      .orders-info p {
        margin: 0;
        color: #6b3f1d;
        font-size: 1.05rem;
        line-height: 1.7;
        max-width: 600px;
        margin: 0 auto;
      }

      @media (max-width: 768px) {
        .public-header {
          padding: 10px 16px;
          min-height: 64px;
        }

        .brand-back {
          font-size: 0.9rem;
          padding: 6px 12px;
        }

        .orders-container {
          padding: 40px 16px;
        }

        .orders-header {
          margin-bottom: 40px;
        }

        .orders-header h1 {
          font-size: 2rem;
        }

        .subtitle {
          font-size: 1rem;
        }

        .orders-grid {
          grid-template-columns: 1fr;
          gap: 16px;
          margin-bottom: 40px;
        }

        .order-card {
          padding: 20px 16px;
          border-radius: 16px;
        }

        .order-emoji {
          font-size: 2.2rem;
        }

        .order-card h3 {
          font-size: 1.1rem;
        }

        .order-card p {
          font-size: 0.9rem;
        }

        .orders-info {
          padding: 24px 16px;
          border-radius: 16px;
        }

        .orders-info p {
          font-size: 0.95rem;
        }
      }
    `
  ]
})
export class PublicOrdersComponent {
  categories: OrderCategory[] = [
    {
      name: 'Tortas',
      description: 'Tortas para aniversários, reuniões, cafés especiais e momentos em família.',
      emoji: '🎂'
    },
    {
      name: 'Bolos',
      description: 'Bolos para o dia a dia ou para deixar sua comemoração mais completa.',
      emoji: '🍰'
    },
    {
      name: 'Rocamboles',
      description: 'Rocamboles doces para servir em fatias, presentear ou compartilhar.',
      emoji: '🍥'
    },
    {
      name: 'Salgados',
      description: 'Salgados para festas, encontros, reuniões e pedidos maiores.',
      emoji: '🥟'
    },
    {
      name: 'Kits de café da manhã',
      description: 'Combinações para presentear ou montar um café especial em casa.',
      emoji: '☕'
    },
    {
      name: 'Doces',
      description: 'Doces para complementar sua mesa, festa ou encomenda especial.',
      emoji: '🧁'
    }
  ];

  private buildWhatsappUrl(message: string): string {
    const phone = '(21) 96582-4610'.replace(/\D/g, '');
    const phoneWithCountryCode = phone.startsWith('55') ? phone : `55${phone}`;
    return `https://wa.me/${phoneWithCountryCode}?text=${encodeURIComponent(message)}`;
  }

  getDoubtUrl(categoryName: string): string {
    return this.buildWhatsappUrl(
      `Olá! Vim pelo site da Padaria Nova Aliança e tenho uma dúvida sobre encomendas de ${categoryName}.`
    );
  }
}
