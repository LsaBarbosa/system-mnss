import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-order-confirmation-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="confirmation-container">
      <div class="card">
        <div class="success-icon">✓</div>
        <h1>Pedido Confirmado!</h1>
        <p class="subtitle">Seu pedido foi recebido com sucesso.</p>

        <div class="order-id-box">
          <span>ID do Pedido:</span>
          <strong>{{ orderId }}</strong>
        </div>

        <div class="status-box">
          <span class="status-icon">🕒</span>
          <div class="status-text">
            <strong>Aguardando confirmação da loja</strong>
            <p>Assim que a loja aceitar seu pedido, iniciaremos o preparo.</p>
          </div>
        </div>

        <div class="actions">
          <a routerLink="/cardapio" class="btn-primary">Fazer outro pedido</a>
          <a routerLink="/" class="btn-secondary">Voltar para a Home</a>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .confirmation-container {
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f8f9fa;
        padding: 20px;
      }

      .card {
        background-color: white;
        padding: 48px;
        border-radius: 24px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
        max-width: 500px;
        width: 100%;
        text-align: center;
      }

      .success-icon {
        width: 80px;
        height: 80px;
        background-color: #2ecc71;
        color: white;
        border-radius: 50%;
        font-size: 40px;
        line-height: 80px;
        margin: 0 auto 24px;
        box-shadow: 0 4px 15px rgba(46, 204, 113, 0.3);
      }

      h1 {
        font-size: 2rem;
        font-weight: 800;
        color: #2c3e50;
        margin-bottom: 8px;
      }

      .subtitle {
        color: #7f8c8d;
        font-size: 1.1rem;
        margin-bottom: 32px;
      }

      .order-id-box {
        background-color: #f1f3f5;
        padding: 16px;
        border-radius: 12px;
        margin-bottom: 32px;
        display: flex;
        flex-direction: column;
        gap: 4px;

        span {
          font-size: 0.85rem;
          color: #95a5a6;
          text-transform: uppercase;
          letter-spacing: 1px;
          font-weight: 600;
        }

        strong {
          font-size: 1.1rem;
          color: #2c3e50;
          word-break: break-all;
        }
      }

      .status-box {
        display: flex;
        align-items: flex-start;
        gap: 16px;
        text-align: left;
        padding: 20px;
        background-color: #fff9db;
        border: 1px solid #ffe066;
        border-radius: 12px;
        margin-bottom: 40px;

        .status-icon {
          font-size: 24px;
        }

        .status-text {
          strong {
            display: block;
            color: #856404;
            margin-bottom: 4px;
          }
          p {
            margin: 0;
            font-size: 0.9rem;
            color: #856404;
            opacity: 0.8;
          }
        }
      }

      .actions {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }

      .btn-primary {
        background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
        color: white;
        padding: 16px;
        border-radius: 12px;
        text-decoration: none;
        font-weight: 700;
        transition: transform 0.2s;

        &:hover {
          transform: translateY(-2px);
        }
      }

      .btn-secondary {
        color: #7f8c8d;
        padding: 12px;
        text-decoration: none;
        font-weight: 600;

        &:hover {
          color: #34495e;
        }
      }
    `
  ]
})
export class OrderConfirmationPageComponent implements OnInit {
  orderId: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.orderId = this.route.snapshot.paramMap.get('id');
  }
}
