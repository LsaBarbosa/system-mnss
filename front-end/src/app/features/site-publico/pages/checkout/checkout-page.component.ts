import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CartService, CartItem } from '../../data-access/cart.service';
import { OrderService, CreateOrderRequest } from '../../data-access/order.service';
import { OnlinePaymentService } from '../../data-access/online-payment.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout-page.component.html',
  styleUrls: ['./checkout-page.component.scss']
})
export class CheckoutPageComponent implements OnInit {
  checkoutForm: FormGroup;
  cartItems$: Observable<CartItem[]>;
  subtotal$: Observable<number>;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private cartService: CartService,
    private orderService: OrderService,
    private paymentService: OnlinePaymentService,
    private router: Router
  ) {
    this.cartItems$ = this.cartService.items$;
    this.subtotal$ = this.cartService.getSubtotal();
    
    this.checkoutForm = this.fb.group({
      customer: this.fb.group({
        name: ['', [Validators.required, Validators.maxLength(150)]],
        phone: ['', [Validators.required, Validators.maxLength(30)]],
        email: ['', [Validators.email, Validators.maxLength(150)]]
      }),
      deliveryType: ['PICKUP', Validators.required],
      address: this.fb.group({
        street: [''],
        number: [''],
        neighborhood: [''],
        city: [''],
        state: [''],
        zipCode: [''],
        complement: [''],
        reference: ['']
      }),
      notes: [''],
      paymentMethod: ['CASH', Validators.required]
    });
  }

  ngOnInit() {
    this.checkoutForm.get('deliveryType')?.valueChanges.subscribe(type => {
      this.updateAddressValidators(type);
    });
  }

  private updateAddressValidators(type: string) {
    const addressGroup = this.checkoutForm.get('address') as FormGroup;
    if (type === 'DELIVERY') {
      addressGroup.get('street')?.setValidators([Validators.required, Validators.maxLength(150)]);
    } else {
      addressGroup.get('street')?.clearValidators();
    }
    addressGroup.get('street')?.updateValueAndValidity();
  }

  onSubmit() {
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const formValue = this.checkoutForm.value;
    
    this.cartService.items$.subscribe(items => {
      const request: CreateOrderRequest = {
        customer: formValue.customer,
        deliveryType: formValue.deliveryType,
        address: formValue.deliveryType === 'DELIVERY' ? formValue.address : undefined,
        items: items.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          observation: item.observation
        })),
        paymentMethod: formValue.paymentMethod
      };

      this.orderService.createOrder(request).subscribe({
        next: (response) => {
          this.cartService.clear();
          
          if (response.paymentMethod === 'ONLINE_PIX') {
            this.router.navigate(['/pagamento', response.id]);
          } else {
            this.router.navigate(['/pedido-confirmado', response.id]);
          }
        },
        error: (err) => {
          this.isSubmitting = false;
          console.error('Error creating order', err);
          alert('Erro ao criar pedido. Por favor, tente novamente.');
        }
      });
    }).unsubscribe();
  }
}
