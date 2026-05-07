import { CommonModule } from '@angular/common';
import type { OnInit } from '@angular/core';
import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ErrorBannerComponent } from '../../../shared/error-banner/error-banner.component';
import type { CashMovementType, PaymentMethod, ProductModel } from '../../../shared/models/domain.models';
import { CashRegisterService } from '../data-access/cash-register.service';
import type { CashRegisterSummaryResponse, CurrentCashRegisterResponse } from '../domain/cash-register.models';
import type { PdvProductGroup } from '../domain/pdv-catalog.models';
import { PdvCatalogService } from '../data-access/pdv-catalog.service';
import { PdvSaleService } from '../data-access/pdv-sale.service';
import type { PdvSale, PdvSaleItem } from '../domain/pdv-sale.models';
import { HardwareService } from '../data-access/hardware.service';
import { KdsService } from '../../kds/data-access/kds.service';
import { AuthService } from '../../../core/auth/auth.service';
import { SyncStatusBadgeComponent } from '../../../shared/sync-status-badge/sync-status-badge.component';

@Component({
  selector: 'mnss-pdv-products-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ErrorBannerComponent, SyncStatusBadgeComponent],
  templateUrl: './pdv-products-page.component.html',
  styleUrl: './pdv-products-page.component.scss'
})
export class PdvProductsPageComponent implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly pdvCatalogService = inject(PdvCatalogService);
  private readonly cashRegisterService = inject(CashRegisterService);
  private readonly pdvSaleService = inject(PdvSaleService);
  private readonly hardwareService = inject(HardwareService);
  private readonly kdsService = inject(KdsService);
  private readonly authService = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  groups: PdvProductGroup[] = [];
  currentCash: CurrentCashRegisterResponse = { open: false, cashRegister: null };
  currentSale: PdvSale | null = null;
  salesHistory: PdvSale[] = [];
  cashSummary: CashRegisterSummaryResponse | null = null;
  cashError: string | null = null;
  cashSuccess: string | null = null;
  saleError: string | null = null;
  saleSuccess: string | null = null;
  changeAmount: string | null = null;
  barcodeError: string | null = null;
  hardwareSuccess: string | null = null;
  showDiscountModal = false;
  showCancelModal = false;
  saleToCancel: string | null = null;

  roles: string[] = [];

  readonly openForm = this.formBuilder.nonNullable.group({
    openingAmount: ['', [Validators.required, Validators.min(0)]],
    notes: ['']
  });

  readonly movementForm = this.formBuilder.nonNullable.group({
    type: ['CASH_OUT' as CashMovementType, Validators.required],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    description: ['', Validators.required]
  });

  readonly closeForm = this.formBuilder.nonNullable.group({
    closingAmount: ['', [Validators.required, Validators.min(0)]],
    notes: ['']
  });

  readonly productFilterForm = this.formBuilder.nonNullable.group({
    name: [''],
    categoryId: ['']
  });

  readonly paymentForm = this.formBuilder.nonNullable.group({
    method: ['CASH' as PaymentMethod, Validators.required],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    transactionId: ['']
  });

  readonly discountForm = this.formBuilder.nonNullable.group({
    type: ['VALUE' as 'VALUE' | 'PERCENT', Validators.required],
    amount: ['', [Validators.required, Validators.min(0)]]
  });

  readonly cancelForm = this.formBuilder.nonNullable.group({
    reason: ['', Validators.required]
  });

  readonly barcodeControl = this.formBuilder.nonNullable.control('');

  ngOnInit(): void {
    this.roles = this.authService.currentUser?.roles ?? [];
    this.loadCash();
    this.loadProducts();
    this.loadSales();
    this.subscribeToKds();
  }

  private subscribeToKds(): void {
    this.kdsService.readyOrders$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(orderId => {
      this.saleSuccess = `Pedido pronto para entrega! ID: ${orderId.substring(0, 8)}`;
    });
  }

  get cashStatusLabel(): string {
    return this.currentCash.open ? 'Caixa aberto' : 'Caixa fechado';
  }

  get canSell(): boolean {
    return this.currentCash.open;
  }

  get canUseCart(): boolean {
    return this.canSell && this.currentSale?.status === 'CREATED';
  }

  get canRegisterPayment(): boolean {
    return this.canUseCart && (this.currentSale?.items.length ?? 0) > 0;
  }

  get canFinalizeSale(): boolean {
    return this.canRegisterPayment && this.currentSale?.remainingAmount === '0.00';
  }

  get discountPreview(): { discount: number; total: number } | null {
    if (!this.currentSale || this.discountForm.invalid) return null;
    const subtotal = Number(this.currentSale.subtotal);
    const amount = Number(this.discountForm.value.amount);
    let discount = 0;
    
    if (this.discountForm.value.type === 'PERCENT') {
      discount = (subtotal * amount) / 100;
    } else {
      discount = amount;
    }

    return {
      discount,
      total: Math.max(0, subtotal - discount + Number(this.currentSale.deliveryFee))
    };
  }

  get categories(): Array<{ id: string; name: string }> {
    return this.groups.map((group) => ({ id: group.category.id, name: group.category.name }));
  }

  get visualDifference(): string {
    const counted = Number(this.closeForm.controls.closingAmount.value || 0);
    const expected = Number(this.cashSummary?.expectedAmount ?? this.currentCash.cashRegister?.openingAmount ?? 0);
    return (counted - expected).toFixed(2);
  }

  openCash(): void {
    this.clearCashMessages();
    if (this.openForm.invalid) {
      this.openForm.markAllAsTouched();
      return;
    }

    const formValue = this.openForm.getRawValue();
    this.cashRegisterService
      .open({
        openingAmount: formValue.openingAmount,
        notes: this.emptyToNull(formValue.notes)
      })
      .subscribe({
        next: (cashRegister) => {
          this.currentCash = { open: true, cashRegister };
          this.cashSuccess = 'Caixa aberto.';
          this.openForm.reset({ openingAmount: '', notes: '' });
          this.loadSummary(cashRegister.id);
        },
        error: () => {
          this.cashError = 'Nao foi possivel abrir o caixa.';
        }
      });
  }

  registerMovement(): void {
    this.clearCashMessages();
    const cashRegisterId = this.currentCash.cashRegister?.id;
    if (!cashRegisterId) {
      this.cashError = 'Abra o caixa antes de registrar movimentacoes.';
      return;
    }
    if (this.movementForm.invalid) {
      this.movementForm.markAllAsTouched();
      return;
    }

    const formValue = this.movementForm.getRawValue();
    this.cashRegisterService
      .createMovement(cashRegisterId, {
        type: formValue.type,
        amount: formValue.amount,
        description: formValue.description.trim()
      })
      .subscribe({
        next: () => {
          this.cashSuccess = formValue.type === 'CASH_OUT' ? 'Sangria registrada.' : 'Suprimento registrado.';
          this.movementForm.reset({ type: 'CASH_OUT', amount: '', description: '' });
          this.loadSummary(cashRegisterId);
        },
        error: () => {
          this.cashError = 'Nao foi possivel registrar a movimentacao.';
        }
      });
  }

  closeCash(): void {
    this.clearCashMessages();
    const cashRegisterId = this.currentCash.cashRegister?.id;
    if (!cashRegisterId) {
      this.cashError = 'Nao ha caixa aberto para fechar.';
      return;
    }
    if (this.closeForm.invalid) {
      this.closeForm.markAllAsTouched();
      return;
    }
    if (this.visualDifference !== '0.00' && !this.closeForm.controls.notes.value.trim()) {
      this.cashError = 'Informe a justificativa da divergencia.';
      return;
    }

    const formValue = this.closeForm.getRawValue();
    this.cashRegisterService
      .close(cashRegisterId, {
        closingAmount: formValue.closingAmount,
        notes: this.emptyToNull(formValue.notes)
      })
      .subscribe({
        next: (summary) => {
          this.cashSummary = summary;
          this.currentCash = { open: false, cashRegister: summary.cashRegister };
          this.currentSale = null;
          this.cashSuccess = 'Caixa fechado.';
          this.closeForm.reset({ closingAmount: '', notes: '' });
        },
        error: () => {
          this.cashError = 'Nao foi possivel fechar o caixa.';
        }
      });
  }

  getPaidAmount(): string {
    if (!this.currentSale) {
      return '0.00';
    }
    const total = parseFloat(String(this.currentSale.totalAmount)) || 0;
    const remaining = parseFloat(String(this.currentSale.remainingAmount)) || 0;
    return (total - remaining).toFixed(2);
  }

  startSale(): void {
    this.clearSaleMessages();
    if (!this.canSell) {
      this.saleError = 'Abra o caixa antes de iniciar a venda.';
      return;
    }

    this.pdvSaleService.createSale().subscribe({
      next: (sale) => {
        this.setCurrentSale(sale);
        this.saleSuccess = 'Venda iniciada.';
        this.loadSales();
      },
      error: () => {
        this.saleError = 'Nao foi possivel iniciar a venda.';
      }
    });
  }

  addProduct(product: ProductModel): void {
    this.clearSaleMessages();
    const saleId = this.currentSale?.id;
    if (!saleId || !this.canUseCart) {
      this.saleError = 'Inicie uma venda antes de adicionar produtos.';
      return;
    }

    this.pdvSaleService.addItem(saleId, { productId: product.id, quantity: '1.000' }).subscribe({
      next: (sale) => {
        this.setCurrentSale(sale);
      },
      error: () => {
        this.saleError = 'Nao foi possivel adicionar o produto.';
      }
    });
  }

  increaseItem(item: PdvSaleItem): void {
    if (!this.canUseCart) {
      return;
    }
    this.updateItemQuantity(item, this.quantityString(Number(item.quantity) + 1));
  }

  decreaseItem(item: PdvSaleItem): void {
    if (!this.canUseCart) {
      return;
    }
    const quantity = Number(item.quantity);
    if (quantity <= 1) {
      return;
    }
    this.updateItemQuantity(item, this.quantityString(quantity - 1));
  }

  canDecrease(item: PdvSaleItem): boolean {
    return this.canUseCart && Number(item.quantity) > 1;
  }

  updateItemQuantity(item: PdvSaleItem, quantity: string): void {
    this.clearSaleMessages();
    const saleId = this.currentSale?.id;
    const normalized = Number(quantity);
    if (!saleId || !this.canUseCart || !Number.isFinite(normalized) || normalized <= 0) {
      this.saleError = 'Quantidade invalida.';
      return;
    }

    this.pdvSaleService.updateItem(saleId, item.id, { quantity: this.quantityString(normalized) }).subscribe({
      next: (sale) => {
        this.setCurrentSale(sale);
      },
      error: () => {
        this.saleError = 'Nao foi possivel alterar a quantidade.';
      }
    });
  }

  removeItem(item: PdvSaleItem): void {
    this.clearSaleMessages();
    const saleId = this.currentSale?.id;
    if (!saleId || !this.canUseCart) {
      return;
    }

    this.pdvSaleService.removeItem(saleId, item.id).subscribe({
      next: (sale) => {
        this.setCurrentSale(sale);
      },
      error: () => {
        this.saleError = 'Nao foi possivel remover o item.';
      }
    });
  }

  registerPayment(): void {
    this.clearSaleMessages();
    const saleId = this.currentSale?.id;
    if (!saleId || !this.canRegisterPayment) {
      this.saleError = 'Adicione itens antes de registrar pagamento.';
      return;
    }
    if (this.paymentForm.invalid) {
      this.paymentForm.markAllAsTouched();
      return;
    }

    const formValue = this.paymentForm.getRawValue();
    this.pdvSaleService
      .pay(saleId, {
        method: formValue.method,
        amount: formValue.amount,
        transactionId: this.emptyToNull(formValue.transactionId),
        gateway: null
      })
      .subscribe({
        next: (payment) => {
          this.changeAmount = Number(payment.changeAmount) > 0 ? payment.changeAmount : null;
          
          if (payment.remainingAmount === '0.00') {
            this.saleSuccess = 'Pagamento concluído. Finalize a venda.';
            this.paymentForm.reset({ method: 'CASH', amount: '', transactionId: '' });
          } else {
            this.saleSuccess = 'Pagamento registrado. Saldo restante: ' + payment.remainingAmount;
            this.paymentForm.reset({ method: 'CASH', amount: payment.remainingAmount, transactionId: '' });
          }
          this.pdvSaleService.getSale(saleId).subscribe((updatedSale) => {
            this.setCurrentSale(updatedSale);
          });
        },
        error: (err) => {
          this.saleError = err.error?.message || 'Nao foi possivel registrar o pagamento.';
        }
      });
  }

  searchProducts(): void {
    this.loadProducts();
  }

  onBarcodeKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Enter') {
      return;
    }
    event.preventDefault();
    const barcode = this.barcodeControl.value.trim();
    if (!barcode) {
      return;
    }
    if (!this.currentSale) {
      this.saleError = 'Inicie uma venda antes de usar o scanner.';
      return;
    }

    this.barcodeError = null;
    this.pdvCatalogService.findProductByBarcode(barcode).subscribe({
      next: (product) => {
        this.barcodeControl.setValue('');
        this.addProduct(product);
      },
      error: () => {
        this.barcodeError = 'Produto nao encontrado para venda.';
      }
    });
  }

  finishSale(): void {
    this.clearSaleMessages();
    const saleId = this.currentSale?.id;
    if (!saleId || !this.canFinalizeSale) return;

    this.pdvSaleService.finish(saleId).subscribe({
      next: (sale) => {
        this.saleSuccess = 'Venda finalizada com sucesso.';
        this.loadSales();
        this.currentSale = null;
        
        if (sale.payments.some(p => p.method === 'CASH')) {
          this.hardwareService.openDrawer().subscribe({
            next: () => this.hardwareSuccess = 'Gaveta acionada automaticamente.',
            error: () => {}
          });
        }
        
        const cashRegisterId = this.currentCash.cashRegister?.id;
        if (cashRegisterId) {
          this.loadSummary(cashRegisterId);
        }
      },
      error: (err) => {
        this.saleError = err.error?.message || 'Nao foi possivel finalizar a venda.';
      }
    });
  }

  applyDiscount(): void {
    if (!this.currentSale || this.discountForm.invalid) return;
    const preview = this.discountPreview;
    if (!preview) return;

    this.pdvSaleService
      .applyDiscount(this.currentSale.id, { amount: preview.discount.toFixed(2) })
      .subscribe({
        next: (sale) => {
          this.setCurrentSale(sale);
          this.showDiscountModal = false;
          this.saleSuccess = 'Desconto aplicado.';
          setTimeout(() => (this.saleSuccess = null), 3000);
        },
        error: (err) => {
          this.saleError = err.error?.message || 'Erro ao aplicar desconto.';
        }
      });
  }

  confirmCancelSale(): void {
    if (!this.saleToCancel || this.cancelForm.invalid) return;

    this.pdvSaleService.cancelSale(this.saleToCancel, { reason: this.cancelForm.controls.reason.value }).subscribe({
      next: () => {
        this.saleSuccess = 'Venda cancelada e valores estornados.';
        this.showCancelModal = false;
        this.saleToCancel = null;
        this.cancelForm.reset();
        this.loadSales();
        
        const cashRegisterId = this.currentCash.cashRegister?.id;
        if (cashRegisterId) {
          this.loadSummary(cashRegisterId);
        }
      },
      error: (err) => {
        this.saleError = err.error?.message || 'Nao foi possivel cancelar a venda.';
      }
    });
  }

  openCancelModal(saleId: string): void {
    this.saleToCancel = saleId;
    this.showCancelModal = true;
  }

  printReceipt(saleId: string): void {
    // A simulação será com toast visual
    this.hardwareSuccess = 'Comprovante enviado para impressão (simulado).';
    setTimeout(() => this.hardwareSuccess = null, 3000);
  }

  private loadCash(): void {
    this.cashRegisterService.current().subscribe({
      next: (currentCash) => {
        this.currentCash = currentCash;
        if (currentCash.cashRegister) {
          this.loadSummary(currentCash.cashRegister.id);
        }
      },
      error: () => {
        this.cashError = 'Nao foi possivel consultar o caixa atual.';
      }
    });
  }

  private loadProducts(): void {
    const filters = this.productFilterForm.getRawValue();
    this.pdvCatalogService
      .listProducts({
        name: this.emptyToNull(filters.name),
        categoryId: this.emptyToNull(filters.categoryId)
      })
      .subscribe((groups) => {
        this.groups = groups;
      });
  }

  private loadSales(): void {
    this.pdvSaleService.listSales().subscribe({
      next: (sales) => {
        this.salesHistory = sales;
      },
      error: () => {
        this.salesHistory = [];
      }
    });
  }

  private loadSummary(cashRegisterId: string): void {
    this.cashRegisterService.summary(cashRegisterId).subscribe({
      next: (summary) => {
        this.cashSummary = summary;
      },
      error: () => {
        this.cashSummary = null;
      }
    });
  }

  private clearCashMessages(): void {
    this.cashError = null;
    this.cashSuccess = null;
  }

  private clearSaleMessages(): void {
    this.saleError = null;
    this.saleSuccess = null;
    this.changeAmount = null;
    this.barcodeError = null;
  }

  private quantityString(quantity: number): string {
    return quantity.toFixed(3);
  }

  private setCurrentSale(sale: PdvSale): void {
    this.currentSale = sale;
    this.paymentForm.patchValue({ amount: sale.remainingAmount }, { emitEvent: false });
  }

  private emptyToNull(value: string): string | null {
    const normalized = value.trim();
    return normalized ? normalized : null;
  }
}
