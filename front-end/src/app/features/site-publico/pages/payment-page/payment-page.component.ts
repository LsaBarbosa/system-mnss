import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OnlinePaymentService, PaymentResponse } from '../../data-access/online-payment.service';
import { OrderService, OrderResponse } from '../../data-access/order.service';
import { ErrorMessageService } from '../../../../core/errors/error-message.service';
import { interval, switchMap, takeWhile } from 'rxjs';

@Component({
  selector: 'app-payment-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './payment-page.component.html',
  styleUrls: ['./payment-page.component.scss']
})
export class PaymentPageComponent implements OnInit {
  orderId: string | null = null;
  payment: PaymentResponse | null = null;
  order: OrderResponse | null = null;
  loading = true;
  error: string | null = null;

  private readonly destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: OnlinePaymentService,
    private orderService: OrderService,
    private errorMessageService: ErrorMessageService
  ) {}

  ngOnInit() {
    this.orderId = this.route.snapshot.paramMap.get('orderId');
    if (this.orderId) {
      this.initPayment(this.orderId);
    } else {
      this.error = 'Pedido não encontrado';
      this.loading = false;
    }
  }

  private initPayment(orderId: string) {
    this.paymentService
      .createPayment({ orderId, method: 'ONLINE_PIX' })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.payment = response;
          this.loading = false;
          this.startStatusPolling(orderId);
        },
        error: () => {
          this.error = 'Erro ao gerar cobrança. Por favor, tente novamente.';
          this.loading = false;
        }
      });
  }

  private startStatusPolling(orderId: string) {
    interval(5000)
      .pipe(
        switchMap(() => this.orderService.getOrder(orderId)),
        takeWhile((order) => order.status !== 'SENT_TO_STORE' && order.status !== 'ACCEPTED', true),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (order) => {
          this.order = order;
          if (order.status === 'SENT_TO_STORE' || order.status === 'ACCEPTED') {
            this.router.navigate(['/pedido-confirmado', orderId]);
          }
        },
        error: () => {
          this.error = 'Erro ao consultar status do pedido.';
        }
      });
  }

  copyPixKey() {
    if (this.payment?.qrCodeCopyPaste) {
      navigator.clipboard.writeText(this.payment.qrCodeCopyPaste);
      this.errorMessageService.showMessage('Chave Pix copiada!');
    }
  }
}
