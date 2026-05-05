import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { OnlinePaymentService, PaymentResponse } from '../../services/online-payment.service';
import { OrderService, OrderResponse } from '../../services/order.service';
import { interval, Subscription, switchMap, takeWhile } from 'rxjs';

@Component({
  selector: 'app-payment-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './payment-page.component.html',
  styleUrls: ['./payment-page.component.scss']
})
export class PaymentPageComponent implements OnInit, OnDestroy {
  orderId: string | null = null;
  payment: PaymentResponse | null = null;
  order: OrderResponse | null = null;
  loading = true;
  error: string | null = null;
  statusSubscription: Subscription | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentService: OnlinePaymentService,
    private orderService: OrderService
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

  ngOnDestroy() {
    this.statusSubscription?.unsubscribe();
  }

  private initPayment(orderId: string) {
    this.paymentService.createPayment({ orderId, method: 'ONLINE_PIX' }).subscribe({
      next: (response) => {
        this.payment = response;
        this.loading = false;
        this.startStatusPolling(orderId);
      },
      error: (err) => {
        console.error('Error creating payment', err);
        this.error = 'Erro ao gerar cobrança. Por favor, tente novamente.';
        this.loading = false;
      }
    });
  }

  private startStatusPolling(orderId: string) {
    // Poll every 5 seconds until status is PAID
    this.statusSubscription = interval(5000)
      .pipe(
        switchMap(() => this.orderService.getOrder(orderId)),
        takeWhile(order => order.status !== 'SENT_TO_STORE' && order.status !== 'ACCEPTED', true)
      )
      .subscribe(order => {
        this.order = order;
        if (order.status === 'SENT_TO_STORE' || order.status === 'ACCEPTED') {
          this.router.navigate(['/pedido-confirmado', orderId]);
        }
      });
  }

  copyPixKey() {
    if (this.payment?.qrCodeCopyPaste) {
      navigator.clipboard.writeText(this.payment.qrCodeCopyPaste);
      alert('Chave Pix copiada!');
    }
  }
}
